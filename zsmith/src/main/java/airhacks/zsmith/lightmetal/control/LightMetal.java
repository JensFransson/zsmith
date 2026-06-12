package airhacks.zsmith.lightmetal.control;

import java.util.ServiceLoader;
import java.util.function.UnaryOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.lightmetal.entity.LightMetalAPICallEvent;
import airhacks.zsmith.logging.control.Log;

public interface LightMetal {

    String MODEL_PROPERTY = "lightmetal.model";
    String MAX_TOKENS_PROPERTY = "lightmetal.max.tokens";
    int DEFAULT_MAX_TOKENS = 4096;

    static String modelPath() {
        return ZCfg.requiredString(MODEL_PROPERTY);
    }

    static int maxTokens() {
        return ZCfg.integer(MAX_TOKENS_PROPERTY, DEFAULT_MAX_TOKENS);
    }

    static JSONObject invoke(String system, JSONArray messages, JSONArray tools, float temperature) {
        var payload = anthropicPayload(system, messages, tools, temperature);
        var payloadString = payload.toString();
        Log.request(payloadString);
        Log.llm(">> " + payloadString);

        var event = new LightMetalAPICallEvent();
        event.begin();
        event.model = payload.getString("model");
        Log.agent("invoking lightmetal model: " + event.model);

        var answer = ChatHolder.get().apply(payloadString);
        var response = new JSONObject(answer);
        populateUsage(event, response);
        logTokens(event);
        if (event.shouldCommit()) {
            event.commit();
        }
        Log.response(answer);
        Log.llm("<< " + answer);
        return response;
    }

    static JSONObject invoke(String system, String user, float temperature) {
        var messages = new JSONArray()
                .put(new JSONObject().put("role", "user").put("content", user));
        return invoke(system, messages, null, temperature);
    }

    private static JSONObject anthropicPayload(String system, JSONArray messages, JSONArray tools, float temperature) {
        var payload = new JSONObject()
                .put("model", modelPath())
                .put("system", system == null ? "" : system)
                .put("messages", messages)
                .put("max_tokens", maxTokens())
                .put("temperature", temperature);
        if (tools != null && !tools.isEmpty()) {
            payload.put("tools", tools);
        }
        return payload;
    }

    private static void populateUsage(LightMetalAPICallEvent event, JSONObject response) {
        event.stopReason = response.optString("stop_reason", null);
        var usage = response.optJSONObject("usage");
        if (usage != null) {
            event.inputTokens = usage.optInt("input_tokens");
            event.outputTokens = usage.optInt("output_tokens");
        }
    }

    private static void logTokens(LightMetalAPICallEvent event) {
        var max = maxTokens();
        var headroom = max - event.outputTokens;
        Log.tokens("in=%d out=%d/%d (headroom=%d)".formatted(
                event.inputTokens, event.outputTokens, max, headroom));
        if (event.outputTokens >= max * 0.9) {
            Log.warning("output tokens (%d) at %.0f%% of max (%d) — response may be truncated"
                    .formatted(event.outputTokens, 100.0 * event.outputTokens / max, max));
        }
    }

    final class ChatHolder {

        static volatile UnaryOperator<String> instance;

        static UnaryOperator<String> get() {
            var local = instance;
            if (local != null) return local;
            synchronized (ChatHolder.class) {
                if (instance == null) {
                    instance = discover();
                }
                return instance;
            }
        }

        @SuppressWarnings("unchecked")
        private static UnaryOperator<String> discover() {
            var found = ServiceLoader.load(UnaryOperator.class)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "no UnaryOperator<String> service found — add lightmetal.jar to the classpath "
                                    + "(see https://github.com/AdamBien/lightmetal)"));
            var chat = (UnaryOperator<String>) found;
            if (found instanceof AutoCloseable closeable) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        closeable.close();
                    } catch (Exception ignored) {
                    }
                }, "lightmetal-shutdown"));
            }
            Log.agent("lightmetal provider discovered: " + found.getClass().getName());
            return chat;
        }
    }
}

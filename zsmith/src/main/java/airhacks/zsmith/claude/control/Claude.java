package airhacks.zsmith.claude.control;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.EnumSet;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import airhacks.zsmith.claude.entity.ClaudeAPICallEvent;
import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.logging.control.Log;



public interface Claude {

    int MAX_TOKENS = 4000;
    String ANTHROPIC_VERSION = ZCfg.requiredString("anthropic.version");
    String ANTHROPIC_API_KEY = ZCfg.requiredString("anthropic.api.key");
    Models defaultModel = Models.CLAUDE_47_OPUS;
    String fallbackModelName = "claude-sonnet-4-6";

    enum Models {
        CLAUDE_47_OPUS("claude-opus-4-7", false),
        CLAUDE_46_OPUS("claude-opus-4-6"),
        CLAUDE_46_SONNET(Claude.fallbackModelName);

        private String modelName;
        private String fallbackModelName;
        private boolean supportsTemperature;

        Models(String modelName, String fallbackModelName, boolean supportsTemperature) {
            this.fallbackModelName = fallbackModelName;
            this.modelName = modelName;
            this.supportsTemperature = supportsTemperature;
        }
        Models(String modelName, boolean supportsTemperature) {
            this(modelName, Claude.fallbackModelName, supportsTemperature);
        }
        Models(String modelName) {
            this(modelName, Claude.fallbackModelName, true);
        }

        public String modelName() {
            return this.modelName;
        }

        public String fallbackModelName(){
            return this.fallbackModelName;
        }

        public boolean supportsTemperature() {
            return this.supportsTemperature;
        }

        boolean matches(String partialName) {
            return this.modelName
                    .toLowerCase()
                    .contains(partialName.toLowerCase());
        }

        public static Models fromSystemProperty() {
            var modelInput = System.getProperty("model");
            return fromPartialMatch(modelInput)
            .orElse(defaultModel);
        }

        public static Optional<Models> fromPartialMatch(String partialName) {
            if(partialName == null)
                return Optional.empty();
            return EnumSet.allOf(Models.class)
                    .stream()
                    .filter(model -> model.matches(partialName))
                    .findAny();
         
        }
    }

    HttpClient client = HttpClient.newHttpClient();
    URI uri = URI.create("https://api.anthropic.com/v1/messages");
    Models currentModel = Models.fromSystemProperty();

    static JSONObject invoke(String system, JSONArray messages, JSONArray tools, float temperature) {
        var payloadJSON = claudeMessage(messages, temperature, system);
        payloadJSON.put("model", currentModel.modelName());
        if (tools != null && !tools.isEmpty()) {
            payloadJSON.put("tools", tools);
        }
        var payload = payloadJSON.toString();
        Log.request(payload);
        Log.llm(">> " + payload);
        var answer = invoke(payload);
        Log.response(answer);
        Log.llm("<< " + answer);
        return new JSONObject(answer);
    }

    public static JSONObject invoke(String system, String user, float temperature) {
        var enclosedPrompt = messagePrompt(user);
        Log.request(enclosedPrompt.toString());
        var payloadJSON = Claude.claudeMessage(enclosedPrompt, temperature, system);
        payloadJSON.put("model", currentModel.modelName());
        var payload = payloadJSON.toString();
        Log.request(payload);
        Log.llm(">> " + payload);
        var answer = invoke(payload);
        Log.response(answer);
        Log.llm("<< " + answer);
        return new JSONObject(answer);
    }

    static JSONObject claudeMessage(JSONArray messages, float temperature, String system) {
        var payload = new JSONObject()
                .put("max_tokens", MAX_TOKENS)
                .put("messages", messages)
                .put("system", system);
        if (currentModel.supportsTemperature()) {
            payload.put("temperature", temperature);
        }
        return payload;
    }

    static JSONArray messagePrompt(String user) {
        return new JSONArray()
                .put(message("user", user));

    }

    static JSONObject message(String role, String content) {
        return new JSONObject()
                .put("role", role)
                .put("content", content);
    }

    /*
     * curl https://api.anthropic.com/v1/messages --header "x-api-key: YOUR_API_KEY"
     * ...
     */
    static String invoke(String message) {
        Log.agent("using claude model: %s".formatted(currentModel.modelName()));
        var body = sendInstrumented(message, currentModel.modelName(), false);
        if (body.statusCode() == 529) {
            Log.error("claude is overloaded, retrying with fallback model: %s".formatted(currentModel.fallbackModelName()));
            var fallbackMessage = replaceModel(message, currentModel.modelName(), currentModel.fallbackModelName());
            body = sendInstrumented(fallbackMessage, currentModel.fallbackModelName(), true);
        }
        if (body.statusCode() != 200) {
            throw new IllegalStateException("claude API error %d: %s".formatted(body.statusCode(), body.body()));
        }
        return body.body();
    }

    static HttpResponse<String> sendInstrumented(String message, String model, boolean fallback) {
        var event = new ClaudeAPICallEvent();
        event.begin();
        var response = send(message);
        event.model = model;
        event.fallback = fallback;
        event.statusCode = response.statusCode();
        populateUsage(event, response);
        logTokens(event);
        if (event.shouldCommit()) {
            event.commit();
        }
        return response;
    }

    static void logTokens(ClaudeAPICallEvent event) {
        if (event.statusCode != 200) return;
        Log.tokens("in=%d out=%d cache_read=%d cache_create=%d".formatted(
            event.inputTokens, event.outputTokens,
            event.cacheReadTokens, event.cacheCreationTokens));
    }

    static void populateUsage(ClaudeAPICallEvent event, HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            return;
        }
        try {
            var json = new JSONObject(response.body());
            event.stopReason = json.optString("stop_reason", null);
            var usage = json.optJSONObject("usage");
            if (usage != null) {
                event.inputTokens = usage.optInt("input_tokens");
                event.outputTokens = usage.optInt("output_tokens");
                event.cacheReadTokens = usage.optInt("cache_read_input_tokens");
                event.cacheCreationTokens = usage.optInt("cache_creation_input_tokens");
            }
        } catch (RuntimeException ignored) {
        }
    }

    static String replaceModel(String message, String originalModel, String fallbackModel) {
        return message.replace(originalModel, fallbackModel);
    }

    static HttpResponse<String> send(String message) {
        var request = HttpRequest.newBuilder(uri)
                .POST(BodyPublishers.ofString(message))
                .header("x-api-key", ANTHROPIC_API_KEY)
                .header("content-type", "application/json")
                .header("anthropic-version", ANTHROPIC_VERSION)
                .build();
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Log.error(e.getMessage());
            throw new IllegalStateException("cannot communicate with claude", e);
        }
    }

}

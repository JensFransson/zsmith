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

    Models defaultModel = Models.CLAUDE_48_OPUS;
    String fallbackModelName = "claude-sonnet-4-7";

    static String apiKey() {
        if (bedrock()) {
            var bedrockKey = ZCfg.string("bedrock.api.key", null);
            if (bedrockKey != null && !bedrockKey.isBlank()) {
                return bedrockKey;
            }
        }
        return ZCfg.requiredString("anthropic.api.key");
    }

    String bedrockVersion = "2023-06-01";

    static String apiVersion() {
        if (bedrock()) {
            return ZCfg.string("anthropic.version", bedrockVersion);
        }
        return ZCfg.requiredString("anthropic.version");
    }

    /**
     * Amazon Bedrock Mantle's {@code bedrock-mantle} endpoint is almost entirely conventional:
     * the only variable parts are the region, the model, and the API key. Selecting
     * {@code llm.provider=bedrock} switches on convention-over-configuration — the scheme,
     * host pattern, path, anthropic-version, and the {@code anthropic.} model prefix are all
     * derived, so the same properties file can hold both the native Anthropic and the Bedrock
     * configuration and flip between them with a single key.
     *
     * @see <a href="https://docs.aws.amazon.com/bedrock/latest/userguide/endpoints.html">Amazon Bedrock endpoints</a>
     */
    static boolean bedrock() {
        return "bedrock".equalsIgnoreCase(ZCfg.string("llm.provider", "claude"));
    }

    static String bedrockRegion() {
        return ZCfg.requiredString("bedrock.region");
    }

    enum Capability { TEMPERATURE, EFFORT, ADAPTIVE_THINKING }

    enum Models {
        CLAUDE_48_OPUS("claude-opus-4-8", Claude.fallbackModelName, 32_000, EnumSet.of(Capability.EFFORT, Capability.ADAPTIVE_THINKING)),
        CLAUDE_47_OPUS("claude-opus-4-7", Claude.fallbackModelName, 32_000, EnumSet.of(Capability.EFFORT, Capability.ADAPTIVE_THINKING)),
        CLAUDE_46_OPUS("claude-opus-4-6", Claude.fallbackModelName, 32_000, EnumSet.of(Capability.EFFORT, Capability.ADAPTIVE_THINKING)),
        CLAUDE_46_SONNET(Claude.fallbackModelName, Claude.fallbackModelName, 64_000, EnumSet.allOf(Capability.class));

        private String modelName;
        private String fallbackModelName;
        private int maxTokens;
        private EnumSet<Capability> capabilities;

        Models(String modelName, String fallbackModelName, int maxTokens, EnumSet<Capability> capabilities) {
            this.modelName = modelName;
            this.fallbackModelName = fallbackModelName;
            this.maxTokens = maxTokens;
            this.capabilities = capabilities;
        }

        public String modelName() {
            return this.modelName;
        }

        public String fallbackModelName(){
            return this.fallbackModelName;
        }

        public boolean supports(Capability capability) {
            return this.capabilities.contains(capability);
        }

        public int maxTokens() {
            return this.maxTokens;
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
    Models currentModel = Models.fromSystemProperty();

    static URI endpoint() {
        if (bedrock()) {
            return URI.create("https://bedrock-mantle.%s.api.aws/anthropic/v1/messages"
                    .formatted(bedrockRegion().trim()));
        }
        var scheme = ZCfg.string("claude.scheme", "https");
        var host = ZCfg.string("claude.host", "api.anthropic.com");
        var port = ZCfg.integer("claude.port", -1);
        var path = ZCfg.string("claude.path", "/v1/messages");
        var authority = port > 0 ? host + ":" + port : host;
        return URI.create("%s://%s%s".formatted(scheme, authority, path));
    }

    static String modelName() {
        var model = ZCfg.string("claude.model", currentModel.modelName());
        if (bedrock() && !model.contains(".")) {
            return "anthropic." + model;
        }
        return model;
    }

    static JSONObject invoke(String system, JSONArray messages, JSONArray tools, float temperature) {
        var payloadJSON = claudeMessage(messages, temperature, system);
        payloadJSON.put("model", modelName());
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
        payloadJSON.put("model", modelName());
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
                .put("max_tokens", currentModel.maxTokens())
                .put("messages", messages)
                .put("system", system);
        if (currentModel.supports(Capability.TEMPERATURE)) {
            payload.put("temperature", temperature);
        }
        var thinking = thinkingConfig();
        if (thinking != null) {
            payload.put("thinking", thinking);
        }
        var outputConfig = outputConfig();
        if (outputConfig != null) {
            payload.put("output_config", outputConfig);
        }
        return payload;
    }

    static JSONObject thinkingConfig() {
        if (!currentModel.supports(Capability.ADAPTIVE_THINKING)) return null;
        var mode = ZCfg.string("claude.thinking", null);
        if (mode == null || mode.isBlank()) return null;
        var thinking = new JSONObject().put("type", mode);
        var display = ZCfg.string("claude.thinking.display", null);
        if (display != null && !display.isBlank() && "adaptive".equals(mode)) {
            thinking.put("display", display);
        }
        return thinking;
    }

    static JSONObject outputConfig() {
        if (!currentModel.supports(Capability.EFFORT)) return null;
        var effort = ZCfg.string("claude.effort", null);
        if (effort == null || effort.isBlank()) return null;
        return new JSONObject().put("effort", effort);
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
        Log.agent("requesting claude model: %s".formatted(currentModel.modelName()));
        var body = sendInstrumented(message, currentModel.modelName(), false);
        if (body.statusCode() == 529) {
            Log.error("claude is overloaded, retrying with fallback model: %s".formatted(currentModel.fallbackModelName()));
            var fallbackMessage = replaceModel(message, currentModel.modelName(), currentModel.fallbackModelName());
            body = sendInstrumented(fallbackMessage, currentModel.fallbackModelName(), true);
        }
        if (body.statusCode() != 200) {
            throw new IllegalStateException("claude API error %d at %s: %s".formatted(body.statusCode(), endpoint(), body.body()));
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
        var max = currentModel.maxTokens();
        var headroom = max - event.outputTokens;
        Log.tokens("in=%d out=%d/%d (headroom=%d) cache_read=%d cache_create=%d".formatted(
            event.inputTokens, event.outputTokens, max, headroom,
            event.cacheReadTokens, event.cacheCreationTokens));
        if (event.outputTokens >= max * 0.9) {
            Log.warning("output tokens (%d) at %.0f%% of max (%d) — response may be truncated"
                .formatted(event.outputTokens, 100.0 * event.outputTokens / max, max));
        }
    }

    static void populateUsage(ClaudeAPICallEvent event, HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            return;
        }
        try {
            var json = new JSONObject(response.body());
            event.stopReason = json.optString("stop_reason", null);
            var servedModel = json.optString("model", null);
            if (servedModel != null && !servedModel.isBlank()) {
                Log.agent("served by claude model: %s".formatted(servedModel));
                event.model = servedModel;
            }
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
        var uri = endpoint();
        Log.agent("claude endpoint: " + uri);
        var authHeader = ZCfg.string("anthropic.auth.header", "x-api-key");
        var builder = HttpRequest.newBuilder(uri)
                .POST(BodyPublishers.ofString(message))
                .header(authHeader, apiKey())
                .header("content-type", "application/json")
                .header("anthropic-version", apiVersion());
        var workspaceId = ZCfg.string("anthropic.workspace.id", null);
        if (workspaceId != null && !workspaceId.isBlank()) {
            builder.header("anthropic-workspace-id", workspaceId);
        }
        var request = builder.build();
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Log.error(e.getMessage());
            throw new IllegalStateException("cannot communicate with claude", e);
        }
    }

}

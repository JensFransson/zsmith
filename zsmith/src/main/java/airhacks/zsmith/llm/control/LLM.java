package airhacks.zsmith.llm.control;

import org.json.JSONArray;
import org.json.JSONObject;

import airhacks.zsmith.claude.control.Claude;
import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.openai.control.OpenAI;

public interface LLM {

    static String provider() {
        return ZCfg.string("llm.provider", "claude");
    }

    static JSONObject invoke(String system, JSONArray messages, JSONArray tools, float temperature) {
        return switch (provider()) {
            case "openai" -> OpenAI.invoke(system, messages, tools, temperature);
            case "claude" -> Claude.invoke(system, messages, tools, temperature);
            default -> throw new IllegalStateException(
                "unknown llm.provider '%s' — expected 'claude' or 'openai'".formatted(provider()));
        };
    }
}

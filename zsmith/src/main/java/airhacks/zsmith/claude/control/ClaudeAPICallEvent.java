package airhacks.zsmith.claude.control;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("airhacks.zsmith.claude.APICall")
@Label("Claude API Call")
@Category({"zsmith", "claude"})
@Description("Single HTTP call to the Anthropic Messages API")
public class ClaudeAPICallEvent extends Event {

    @Label("Model")
    String model;

    @Label("Fallback Attempt")
    boolean fallback;

    @Label("HTTP Status")
    int statusCode;

    @Label("Stop Reason")
    String stopReason;

    @Label("Input Tokens")
    int inputTokens;

    @Label("Output Tokens")
    int outputTokens;

    @Label("Cache Read Tokens")
    int cacheReadTokens;

    @Label("Cache Creation Tokens")
    int cacheCreationTokens;
}

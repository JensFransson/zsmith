import org.json.JSONObject;

import airhacks.zsmith.tools.control.UserConfirmationTool;

void main() {
    // tool name is "user_confirmation"
    var tool = new UserConfirmationTool(prompt -> "yes");
    if (!"user_confirmation".equals(tool.toolName()))
        throw new AssertionError("expected 'user_confirmation' but got: " + tool.toolName());

    // description is non-empty
    if (tool.description() == null || tool.description().isBlank())
        throw new AssertionError("description should be non-empty");

    // input schema contains question and required
    var schema = tool.inputSchema();
    if (!schema.contains("\"question\""))
        throw new AssertionError("inputSchema should contain '\"question\"'");
    if (!schema.contains("\"required\""))
        throw new AssertionError("inputSchema should contain '\"required\"'");

    // missing question throws IllegalArgumentException
    try {
        tool.execute(new JSONObject());
        throw new AssertionError("expected IllegalArgumentException for missing question");
    } catch (IllegalArgumentException expected) {
    }

    // empty question throws IllegalArgumentException
    try {
        tool.execute(new JSONObject().put("question", ""));
        throw new AssertionError("expected IllegalArgumentException for empty question");
    } catch (IllegalArgumentException expected) {
    }

    // "yes" input returns "yes"
    var yesResult = new UserConfirmationTool(prompt -> "yes")
            .execute(new JSONObject().put("question", "Proceed?"));
    if (!"yes".equals(yesResult))
        throw new AssertionError("expected 'yes' but got: " + yesResult);

    // "y" input returns "yes"
    var yResult = new UserConfirmationTool(prompt -> "y")
            .execute(new JSONObject().put("question", "Proceed?"));
    if (!"yes".equals(yResult))
        throw new AssertionError("expected 'yes' but got: " + yResult);

    // non-affirmative input returns "no"
    var maybeResult = new UserConfirmationTool(prompt -> "maybe")
            .execute(new JSONObject().put("question", "Proceed?"));
    if (!"no".equals(maybeResult))
        throw new AssertionError("expected 'no' but got: " + maybeResult);

    // explicit "no" returns "no"
    var noResult = new UserConfirmationTool(prompt -> "no")
            .execute(new JSONObject().put("question", "Proceed?"));
    if (!"no".equals(noResult))
        throw new AssertionError("expected 'no' but got: " + noResult);
}

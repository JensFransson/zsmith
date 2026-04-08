import org.json.JSONObject;

import airhacks.zsmith.tools.control.LinkCheckerTool;

void main() {
    var tool = new LinkCheckerTool();

    // no-arg constructor creates valid instance
    if (tool == null)
        throw new AssertionError("no-arg constructor should create a non-null instance");

    // tool name is "check_link"
    if (!"check_link".equals(tool.toolName()))
        throw new AssertionError("expected 'check_link' but got: " + tool.toolName());

    // description is non-empty
    if (tool.description() == null || tool.description().isBlank())
        throw new AssertionError("description should be non-empty");

    // input schema contains url and required
    var schema = tool.inputSchema();
    if (!schema.contains("\"url\""))
        throw new AssertionError("inputSchema should contain '\"url\"'");
    if (!schema.contains("\"required\""))
        throw new AssertionError("inputSchema should contain '\"required\"'");

    // missing url returns error
    var missingResult = tool.execute(new JSONObject());
    if (!"Error: Missing required parameter: url".equals(missingResult))
        throw new AssertionError("expected error for missing url but got: " + missingResult);

    // malformed url returns error
    var malformedResult = tool.execute(new JSONObject().put("url", "not a valid url"));
    if (!malformedResult.startsWith("Error:"))
        throw new AssertionError("expected error for malformed url but got: " + malformedResult);

    // url without scheme returns "Error: Invalid URL"
    var noScheme = tool.execute(new JSONObject().put("url", "missing-scheme"));
    if (!"Error: Invalid URL".equals(noScheme))
        throw new AssertionError("expected 'Error: Invalid URL' but got: " + noScheme);
}

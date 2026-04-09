import org.json.JSONObject;

import airhacks.zsmith.tools.control.LaunchAppTool;

void main() {
    var tool = new LaunchAppTool("open_in_editor", "Opens a file in VS Code", "echo");

    // tool definition
    assert "open_in_editor".equals(tool.toolName()) : "expected 'open_in_editor' but got: " + tool.toolName();
    assert "Opens a file in VS Code".equals(tool.description()) : "unexpected description: " + tool.description();
    assert !tool.inputSchema().isBlank() : "inputSchema should not be blank";

    // execute with arguments
    var result = tool.execute(new JSONObject().put("arguments", "hello world"));
    assert "hello world".equals(result) : "expected 'hello world' but got: " + result;

    // execute with empty arguments
    var emptyResult = tool.execute(new JSONObject().put("arguments", ""));
    assert emptyResult.isEmpty() : "expected empty output but got: " + emptyResult;

    // execute without arguments key
    var noArgResult = tool.execute(new JSONObject());
    assert noArgResult.isEmpty() : "expected empty output but got: " + noArgResult;

    // non-existent command
    var badTool = new LaunchAppTool("bad", "bad", "/nonexistent/command");
    var badResult = badTool.execute(new JSONObject().put("arguments", "test"));
    assert badResult.contains("Error") : "expected error but got: " + badResult;

    // non-zero exit code
    var failTool = new LaunchAppTool("fail", "fail", "sh");
    var failResult = failTool.execute(new JSONObject().put("arguments", "-c exit\u00201"));
    assert failResult.contains("exited with code 1") : "expected 'exited with code 1' but got: " + failResult;
}

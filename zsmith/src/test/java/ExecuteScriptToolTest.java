import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;

import org.json.JSONObject;

import airhacks.zsmith.tools.control.ExecuteScriptTool;

void main() throws IOException {
    var tool = new ExecuteScriptTool();
    var tempDir = Files.createTempDirectory("zunit-execscript");
    try {
        // tool definition
        if (!"execute_script".equals(tool.toolName()))
            throw new AssertionError("expected 'execute_script' but got: " + tool.toolName());
        if (tool.description().isBlank())
            throw new AssertionError("description should not be blank");
        if (tool.inputSchema().isBlank())
            throw new AssertionError("inputSchema should not be blank");

        // execute successful script
        var hello = createScript(tempDir, "hello.sh", "#!/bin/sh\necho hello world");
        var helloResult = tool.execute(input(hello));
        if (!"hello world".equals(helloResult))
            throw new AssertionError("expected 'hello world' but got: " + helloResult);

        // missing script
        var missingResult = tool.execute(input(Path.of("/nonexistent/script.sh")));
        if (!missingResult.contains("Script not found"))
            throw new AssertionError("expected 'Script not found' but got: " + missingResult);

        // non-executable script
        var noperm = tempDir.resolve("noperm.sh");
        Files.writeString(noperm, "#!/bin/sh\necho hello");
        Files.setPosixFilePermissions(noperm, PosixFilePermissions.fromString("rw-r--r--"));
        var nopermResult = tool.execute(input(noperm));
        if (!nopermResult.contains("Script not executable"))
            throw new AssertionError("expected 'Script not executable' but got: " + nopermResult);

        // non-zero exit code
        var fail = createScript(tempDir, "fail.sh", "#!/bin/sh\necho partial\nexit 1");
        var failResult = tool.execute(input(fail));
        if (!failResult.contains("partial"))
            throw new AssertionError("expected 'partial' in output but got: " + failResult);
        if (!failResult.contains("exited with code 1"))
            throw new AssertionError("expected 'exited with code 1' but got: " + failResult);

        // timeout
        var slow = createScript(tempDir, "slow.sh", "#!/bin/sh\nsleep 60");
        var fastTool = new ExecuteScriptTool(1);
        var timeoutResult = fastTool.execute(input(slow));
        if (!timeoutResult.contains("timed out"))
            throw new AssertionError("expected 'timed out' but got: " + timeoutResult);
    } finally {
        try (var walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}

static Path createScript(Path dir, String name, String content) throws IOException {
    var script = dir.resolve(name);
    Files.writeString(script, content);
    Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));
    return script;
}

static JSONObject input(Path scriptPath) {
    return new JSONObject().put("path", scriptPath.toString());
}

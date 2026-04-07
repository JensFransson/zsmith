package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ExecuteScriptToolTest {

    ExecuteScriptTool tool = new ExecuteScriptTool();

    @Test
    void toolDefinition() {
        assertEquals("execute_script", tool.toolName());
        assertFalse(tool.description().isBlank());
        assertFalse(tool.inputSchema().isBlank());
    }

    @Test
    void executeSuccessfulScript(@TempDir Path tempDir) throws IOException {
        var script = createScript(tempDir, "hello.sh", "#!/bin/sh\necho hello world");
        var result = tool.execute(input(script));
        assertEquals("hello world", result);
    }

    @Test
    void executeMissingScript() {
        var result = tool.execute(input(Path.of("/nonexistent/script.sh")));
        assertTrue(result.contains("Script not found"));
    }

    @Test
    void executeNonExecutableScript(@TempDir Path tempDir) throws IOException {
        var script = tempDir.resolve("noperm.sh");
        Files.writeString(script, "#!/bin/sh\necho hello");
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rw-r--r--"));

        var result = tool.execute(input(script));
        assertTrue(result.contains("Script not executable"));
    }

    @Test
    void scriptWithNonZeroExitCode(@TempDir Path tempDir) throws IOException {
        var script = createScript(tempDir, "fail.sh", "#!/bin/sh\necho partial\nexit 1");
        var result = tool.execute(input(script));
        assertTrue(result.contains("partial"));
        assertTrue(result.contains("exited with code 1"));
    }

    @Test
    void scriptTimesOut(@TempDir Path tempDir) throws IOException {
        var script = createScript(tempDir, "slow.sh", "#!/bin/sh\nsleep 60");
        var fastTool = new ExecuteScriptTool(1);
        var result = fastTool.execute(input(script));
        assertTrue(result.contains("timed out"));
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
}

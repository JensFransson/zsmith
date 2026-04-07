package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import airhacks.zsmith.logging.control.Log;

public class ExecuteScriptTool implements Tool {

    static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final int timeoutSeconds;

    public ExecuteScriptTool() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }

    public ExecuteScriptTool(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String toolName() {
        return "execute_script";
    }

    @Override
    public String description() {
        return "Executes a script and returns its output";
    }

    enum Field { path }

    @Override
    public String inputSchema() {
        return Tool.schema(Prop.string(Field.path, "Path to the script to execute"));
    }

    @Override
    public String execute(JSONObject input) {
        var scriptPath = Path.of(input.getString(Field.path.name()));
        if (!Files.isRegularFile(scriptPath)) {
            return "Error: Script not found: " + scriptPath;
        }
        if (!Files.isExecutable(scriptPath)) {
            return "Error: Script not executable: " + scriptPath;
        }
        try {
            Log.tool("executing script: " + scriptPath);
            var process = new ProcessBuilder(scriptPath.toAbsolutePath().toString())
                    .directory(Path.of(System.getProperty("user.dir")).toFile())
                    .redirectErrorStream(true)
                    .start();

            var output = new String(process.getInputStream().readAllBytes());
            var completed = process.waitFor(this.timeoutSeconds, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                return output + "\nError: Script timed out after %ds".formatted(this.timeoutSeconds);
            }

            var exitCode = process.exitValue();
            if (exitCode != 0) {
                return output + "\nError: Script exited with code %d".formatted(exitCode);
            }

            return output.strip();
        } catch (IOException e) {
            return "Error: Script execution failed: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Script interrupted";
        }
    }
}

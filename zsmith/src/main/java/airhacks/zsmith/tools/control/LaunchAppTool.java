package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.logging.control.Log;

public class LaunchAppTool implements Tool {

    static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final String name;
    private final String toolDescription;
    private final String command;
    private final int timeoutSeconds;

    public LaunchAppTool(String name, String description, String command) {
        this(name, description, command, DEFAULT_TIMEOUT_SECONDS);
    }

    public LaunchAppTool(String name, String description, String command, int timeoutSeconds) {
        this.name = name;
        this.toolDescription = description;
        this.command = command;
        this.timeoutSeconds = timeoutSeconds;
    }

    public static LaunchAppTool fromConfig() {
        return new LaunchAppTool(
                ZCfg.requiredString("launch.tool.name"),
                ZCfg.requiredString("launch.tool.description"),
                ZCfg.requiredString("launch.command"));
    }

    @Override
    public String toolName() {
        return this.name;
    }

    @Override
    public String description() {
        return this.toolDescription;
    }

    enum Field { arguments }

    @Override
    public String inputSchema() {
        return Tool.schema(Prop.string(Field.arguments, "Arguments to pass to the application"));
    }

    @Override
    public String execute(JSONObject input) {
        var arguments = input.optString(Field.arguments.name(), "");
        var commandLine = buildCommandLine(arguments);
        try {
            Log.tool("launching: " + String.join(" ", commandLine));
            var process = new ProcessBuilder(commandLine)
                    .directory(java.nio.file.Path.of(System.getProperty("user.dir")).toFile())
                    .redirectErrorStream(true)
                    .start();

            var output = new String(process.getInputStream().readAllBytes());
            var completed = process.waitFor(this.timeoutSeconds, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                return output + "\nError: Command timed out after %ds".formatted(this.timeoutSeconds);
            }

            var exitCode = process.exitValue();
            if (exitCode != 0) {
                return output + "\nError: Command exited with code %d".formatted(exitCode);
            }

            return output.strip();
        } catch (IOException e) {
            return "Error: Command execution failed: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Command interrupted";
        }
    }

    List<String> buildCommandLine(String arguments) {
        var parts = new ArrayList<String>();
        parts.add(this.command);
        if (arguments != null && !arguments.isBlank()) {
            parts.addAll(List.of(arguments.trim().split("\\s+")));
        }
        return parts;
    }
}

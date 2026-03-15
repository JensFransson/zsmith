package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class WriteFileTool implements Tool {

    static final String SANDBOX_PATH_KEY = "sandbox.path";

    private final SandboxedFileSystem fs;

    public WriteFileTool() {
        this(new SandboxedFileSystem(Path.of(ZCfg.requiredString(SANDBOX_PATH_KEY))));
    }

    public WriteFileTool(SandboxedFileSystem fs) {
        this.fs = fs;
    }

    public static WriteFileTool fromConfig() {
        return new WriteFileTool();
    }

    public static WriteFileTool of(String sandboxPath) {
        return new WriteFileTool(new SandboxedFileSystem(Path.of(sandboxPath)));
    }

    @Override
    public String toolName() {
        return "write_file";
    }

    @Override
    public String description() {
        return "Writes content to a file within the sandbox directory";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "path": {
                            "type": "string",
                            "description": "Relative path to the file to write"
                        },
                        "content": {
                            "type": "string",
                            "description": "Content to write to the file"
                        }
                    },
                    "required": ["path", "content"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has("path")) {
            return "Error: Missing required parameter: path";
        }
        if (!input.has("content")) {
            return "Error: Missing required parameter: content";
        }
        var path = input.getString("path");
        try {
            this.fs.writeFile(path, input.getString("content"));
            return "File written successfully: " + path;
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path";
        } catch (RuntimeException e) {
            return "Error: Could not write file";
        }
    }
}

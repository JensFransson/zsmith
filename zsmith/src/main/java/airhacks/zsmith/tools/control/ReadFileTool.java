package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class ReadFileTool implements Tool {

    static final String SANDBOX_PATH_KEY = "sandbox.path";

    private final SandboxedFileSystem fs;

    public ReadFileTool() {
        this(new SandboxedFileSystem(Path.of(ZCfg.requiredString(SANDBOX_PATH_KEY))));
    }

    public ReadFileTool(SandboxedFileSystem fs) {
        this.fs = fs;
    }

    @Override
    public String toolName() {
        return "read_file";
    }

    @Override
    public String description() {
        return "Reads the contents of a file within the sandbox directory";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "path": {
                            "type": "string",
                            "description": "Relative path to the file to read"
                        }
                    },
                    "required": ["path"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has("path")) {
            return "Error: Missing required parameter: path";
        }
        try {
            return this.fs.readFile(input.getString("path"));
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path";
        }
    }
}

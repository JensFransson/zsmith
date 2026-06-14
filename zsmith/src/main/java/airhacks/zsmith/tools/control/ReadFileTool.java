package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class ReadFileTool implements Tool {

    private final SandboxedFileSystem fs;

    public ReadFileTool(SandboxedFileSystem fs) {
        this.fs = fs;
    }

    public static ReadFileTool of(String sandboxPath) {
        var path = Path.of(sandboxPath);
        return new ReadFileTool(new SandboxedFileSystem(path));
    }

    @Override
    public String toolName() {
        return "read_file";
    }

    @Override
    public String description() {
        return "Reads the contents of a file within the sandbox directory";
    }

    enum Field { path }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(Prop.string(Field.path, "Relative path to the file to read"));
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has(Field.path.name())) {
            return "Error: Missing required parameter: path";
        }
        try {
            return this.fs.readFile(input.getString(Field.path.name()));
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path";
        }
    }
}

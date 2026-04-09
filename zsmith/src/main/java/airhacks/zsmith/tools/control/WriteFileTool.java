package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class WriteFileTool implements Tool {

    private final SandboxedFileSystem fs;

    public WriteFileTool(SandboxedFileSystem fs) {
        this.fs = fs;
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

    enum Field { path, content }

    @Override
    public String inputSchema() {
        return Tool.schema(
                Prop.string(Field.path, "Relative path to the file to write"),
                Prop.string(Field.content, "Content to write to the file")
        );
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has(Field.path.name())) {
            return "Error: Missing required parameter: path";
        }
        if (!input.has(Field.content.name())) {
            return "Error: Missing required parameter: content";
        }
        var path = input.getString(Field.path.name());
        try {
            this.fs.writeFile(path, input.getString(Field.content.name()));
            return "File written successfully: " + path;
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path";
        } catch (RuntimeException e) {
            return "Error: Could not write file";
        }
    }
}

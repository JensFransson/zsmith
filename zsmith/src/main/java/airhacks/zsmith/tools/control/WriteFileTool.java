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
        return "Writes content to a file inside the agent's sandbox directory. "
                + "Path must be relative to the sandbox root; absolute paths and \"..\" segments are rejected. "
                + "Overwrites the file by default; pass append=\"true\" to append. "
                + "Creates missing parent directories. "
                + "Use write_any_file for paths outside the sandbox.";
    }

    enum Field { path, content, append }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(
                Prop.string(Field.path, "Relative path to the file to write (sandboxed)"),
                Prop.string(Field.content, "Content to write to the file"),
                Prop.stringEnum(Field.append, "Append to existing file instead of overwriting", "true", "false").optional()
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
        var append = input.has(Field.append.name())
                && Boolean.parseBoolean(input.getString(Field.append.name()));
        try {
            this.fs.writeFile(path, input.getString(Field.content.name()), append);
            return (append ? "Appended to file: " : "File written successfully: ") + path;
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path: " + e.getMessage();
        } catch (RuntimeException e) {
            return e.getMessage() != null ? e.getMessage() : "Error: Could not write file";
        }
    }
}

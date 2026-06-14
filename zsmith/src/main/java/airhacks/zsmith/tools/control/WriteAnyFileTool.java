package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.json.JSONObject;

public class WriteAnyFileTool implements Tool {

    @Override
    public String toolName() {
        return "write_any_file";
    }

    @Override
    public String description() {
        return "Writes content to a file at any absolute path on the filesystem. "
                + "Overwrites the file by default; pass append=\"true\" to append. "
                + "Creates missing parent directories. "
                + "Use write_file for sandboxed writes; use this tool for paths outside the agent sandbox.";
    }

    enum Field { path, content, append }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(
                Prop.string(Field.path, "Absolute path to the file to write"),
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
        var pathString = input.getString(Field.path.name());
        var content = input.getString(Field.content.name());
        var append = input.has(Field.append.name())
                && Boolean.parseBoolean(input.getString(Field.append.name()));

        Path path;
        try {
            path = Path.of(pathString);
            if (!path.isAbsolute()) {
                return "Error: Path must be absolute: " + pathString;
            }
        } catch (Exception e) {
            return "Error: Invalid path: " + e.getMessage();
        }

        try {
            var parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (append) {
                Files.writeString(path, content,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                return "Appended to file: " + path;
            }
            Files.writeString(path, content);
            return "File written successfully: " + path;
        } catch (IOException e) {
            return "Error: Could not write file: " + e.getMessage();
        }
    }
}

package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

public class ReadAnyFileTool implements Tool {

    @Override
    public String toolName() {
        return "read_any_file";
    }

    @Override
    public String description() {
        return "Reads a file from any location on the filesystem";
    }

    enum Field { path }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(Prop.string(Field.path, "Absolute path to the file to read"));
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has(Field.path.name())) {
            return "Error: Missing required parameter: path";
        }
        var path = Path.of(input.getString(Field.path.name()));
        if (!Files.exists(path)) {
            return "Error: File not found: " + path;
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "Error: Could not read file: " + e.getMessage();
        }
    }
}

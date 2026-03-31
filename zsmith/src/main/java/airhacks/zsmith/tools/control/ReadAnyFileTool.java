package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.json.JSONObject;

public class ReadAnyFileTool implements Tool {

    private final Function<String, String> promptFunction;

    public ReadAnyFileTool() {
        this(IO::readln);
    }

    public ReadAnyFileTool(Function<String, String> promptFunction) {
        this.promptFunction = promptFunction;
    }

    @Override
    public String toolName() {
        return "read_any_file";
    }

    @Override
    public String description() {
        return "Reads a file from any location on the filesystem after user confirmation";
    }

    enum Field { path }

    @Override
    public String inputSchema() {
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
        var answer = promptFunction.apply("Allow reading " + path + "? (yes/no): ");
        if (!"yes".equalsIgnoreCase(answer) && !"y".equalsIgnoreCase(answer)) {
            return "Denied: user rejected file access";
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "Error: Could not read file: " + e.getMessage();
        }
    }
}

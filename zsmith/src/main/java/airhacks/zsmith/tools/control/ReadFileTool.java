package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import org.json.JSONObject;

import airhacks.zsmith.tools.boundary.SandboxedFileSystem;

public interface ReadFileTool {

    enum Field { path }

    static Tool of(String sandboxPath) {
        return create(new SandboxedFileSystem(Path.of(sandboxPath)));
    }

    static Tool create(SandboxedFileSystem fs) {
        return Tool.of(
                "read_file",
                "Reads the contents of a file within the sandbox directory",
                Tool.schema(Tool.Prop.string(Field.path, "Relative path to the file to read")),
                input -> run(input, fs));
    }

    private static String run(JSONObject input, SandboxedFileSystem fs) {
        if (!input.has(Field.path.name())) {
            return "Error: Missing required parameter: path";
        }
        try {
            return fs.readFile(input.getString(Field.path.name()));
        } catch (IllegalArgumentException e) {
            return "Error: Invalid path";
        }
    }
}

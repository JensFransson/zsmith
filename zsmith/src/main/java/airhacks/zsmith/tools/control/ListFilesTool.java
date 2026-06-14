package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class ListFilesTool implements Tool {

    SandboxedFileSystem fs;

    public ListFilesTool(SandboxedFileSystem fs) {
        this.fs = fs;
    }

    public static ListFilesTool of(String sandboxPath) {
        return new ListFilesTool(new SandboxedFileSystem(Path.of(sandboxPath)));
    }

    @Override
    public String toolName() {
        return "list_files";
    }

    @Override
    public String description() {
        return "Lists all files within the sandbox directory";
    }

    @Override
    public JSONObject inputSchema() {
        return Tool.emptySchema();
    }

    @Override
    public String execute(JSONObject input) {
        return this.fs.listFiles();
    }
}

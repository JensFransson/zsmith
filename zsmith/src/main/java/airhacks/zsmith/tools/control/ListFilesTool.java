package airhacks.zsmith.tools.control;

import java.nio.file.Path;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.tools.boundary.SandboxedFileSystem;
import org.json.JSONObject;

public class ListFilesTool implements Tool {

    static final String SANDBOX_PATH_KEY = "sandbox.path";

    SandboxedFileSystem fs;

    public ListFilesTool() {
        var path = Path.of(ZCfg.requiredString(SANDBOX_PATH_KEY));
        var fileSystem = new SandboxedFileSystem(path);
        this(fileSystem);
    }

    public ListFilesTool(SandboxedFileSystem fs) {
        this.fs = fs;
    }

    public static ListFilesTool fromConfig() {
        return new ListFilesTool();
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
    public String inputSchema() {
        return Tool.emptySchema();
    }

    @Override
    public String execute(JSONObject input) {
        return this.fs.listFiles();
    }
}

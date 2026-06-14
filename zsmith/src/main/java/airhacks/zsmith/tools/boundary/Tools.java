package airhacks.zsmith.tools.boundary;

import org.json.JSONObject;

import airhacks.zsmith.tools.control.CalculatorTool;
import airhacks.zsmith.tools.control.CurrentTimeTool;
import airhacks.zsmith.tools.control.ExecuteScriptTool;
import airhacks.zsmith.tools.control.FetchUrlTool;
import airhacks.zsmith.tools.control.LinkCheckerTool;
import airhacks.zsmith.tools.control.ReadAnyFileTool;
import airhacks.zsmith.tools.control.ReadClipboardTool;
import airhacks.zsmith.tools.control.Tool;
import airhacks.zsmith.tools.control.UserConfirmationTool;
import airhacks.zsmith.tools.control.UserMessageTool;
import airhacks.zsmith.tools.control.UserQuestionTool;
import airhacks.zsmith.tools.control.WriteAnyFileTool;
import airhacks.zsmith.tools.control.WriteClipboardTool;

public enum Tools implements Tool {

    CALCULATOR(CalculatorTool.create()),
    CURRENT_TIME(CurrentTimeTool.create()),
    READ_CLIPBOARD(new ReadClipboardTool()),
    WRITE_CLIPBOARD(new WriteClipboardTool()),
    READ_ANY_FILE(new ReadAnyFileTool()),
    WRITE_ANY_FILE(new WriteAnyFileTool()),
    LINK_CHECKER(new LinkCheckerTool()),
    FETCH_URL(new FetchUrlTool()),
    USER_CONFIRMATION(new UserConfirmationTool()),
    USER_MESSAGE(new UserMessageTool()),
    USER_QUESTION(new UserQuestionTool()),
    EXECUTE_SCRIPT(new ExecuteScriptTool());

    private final Tool delegate;

    Tools(Tool delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toolName() { return delegate.toolName(); }

    @Override
    public String description() { return delegate.description(); }

    @Override
    public JSONObject inputSchema() { return delegate.inputSchema(); }

    @Override
    public String execute(JSONObject input) { return delegate.execute(input); }

    @Override
    public boolean parallel() { return delegate.parallel(); }
}

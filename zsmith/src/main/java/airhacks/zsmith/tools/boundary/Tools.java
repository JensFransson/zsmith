package airhacks.zsmith.tools.boundary;

import org.json.JSONObject;

import airhacks.zsmith.tools.control.CalculatorTool;
import airhacks.zsmith.tools.control.CurrentTimeTool;
import airhacks.zsmith.tools.control.LinkCheckerTool;
import airhacks.zsmith.tools.control.ReadClipboardTool;
import airhacks.zsmith.tools.control.Tool;
import airhacks.zsmith.tools.control.UserConfirmationTool;

public enum Tools implements Tool {

    CALCULATOR(new CalculatorTool()),
    CURRENT_TIME(new CurrentTimeTool()),
    READ_CLIPBOARD(new ReadClipboardTool()),
    LINK_CHECKER(new LinkCheckerTool()),
    USER_CONFIRMATION(new UserConfirmationTool());

    private final Tool delegate;

    Tools(Tool delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toolName() { return delegate.toolName(); }

    @Override
    public String description() { return delegate.description(); }

    @Override
    public String inputSchema() { return delegate.inputSchema(); }

    @Override
    public String execute(JSONObject input) { return delegate.execute(input); }
}

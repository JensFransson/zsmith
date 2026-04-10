import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.subagent.control.SubAgentTool;

void main() {
    var child = new Agent("researcher", "You are a research assistant.");
    var tool = new SubAgentTool(child);

    // tool name derived from agent name
    assert "delegate_to_researcher".equals(tool.toolName())
            : "expected 'delegate_to_researcher' but got: " + tool.toolName();

    // description contains agent name
    assert tool.description().contains("researcher")
            : "description should mention agent name: " + tool.description();

    // input schema is valid
    assert !tool.inputSchema().isBlank() : "inputSchema should not be blank";
    assert tool.inputSchema().contains("task") : "inputSchema should have 'task' field";

    // tool definition shape
    var definition = tool.toToolDefinition();
    assert "delegate_to_researcher".equals(definition.getString("name"));
    assert definition.has("input_schema");
    assert definition.has("description");

    // custom name and description
    var customTool = new SubAgentTool(child, "ask_expert", "Asks the domain expert");
    assert "ask_expert".equals(customTool.toolName())
            : "expected 'ask_expert' but got: " + customTool.toolName();
    assert "Asks the domain expert".equals(customTool.description())
            : "unexpected description: " + customTool.description();

    // depth guard
    var shallowTool = new SubAgentTool(child, "shallow", "test", 0);
    var result = shallowTool.execute(new org.json.JSONObject().put("task", "test"));
    assert result.contains("Error") && result.contains("depth")
            : "expected depth error but got: " + result;
}

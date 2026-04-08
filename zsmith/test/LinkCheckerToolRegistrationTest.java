import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.tools.boundary.Tools;
import airhacks.zsmith.tools.control.CalculatorTool;
import airhacks.zsmith.tools.control.LinkCheckerTool;

void main() {
    // LinkCheckerTool can be registered via withTool()
    var agent = new Agent().withSystemPrompt("You are a helpful assistant.")
            .withTool(new LinkCheckerTool());
    if (!agent.tools().containsKey("check_link"))
        throw new AssertionError("agent should contain 'check_link' tool");

    // can be registered alongside other tools
    var multi = new Agent().withSystemPrompt("You are a helpful assistant.")
            .withTool(new CalculatorTool())
            .withTool(new LinkCheckerTool());
    if (!multi.tools().containsKey("check_link"))
        throw new AssertionError("agent should contain 'check_link' tool");
    if (!multi.tools().containsKey("calculator"))
        throw new AssertionError("agent should contain 'calculator' tool");
    if (multi.tools().size() < 2)
        throw new AssertionError("agent should have at least 2 tools but got " + multi.tools().size());

    // Tools enum constants work
    var enumAgent = new Agent().withSystemPrompt("You are a helpful assistant.")
            .withTools(Tools.CALCULATOR, Tools.LINK_CHECKER);
    if (!enumAgent.tools().containsKey("check_link"))
        throw new AssertionError("agent should contain 'check_link' via enum");
    if (!enumAgent.tools().containsKey("calculator"))
        throw new AssertionError("agent should contain 'calculator' via enum");
    if (enumAgent.tools().size() < 2)
        throw new AssertionError("agent should have at least 2 tools but got " + enumAgent.tools().size());
}

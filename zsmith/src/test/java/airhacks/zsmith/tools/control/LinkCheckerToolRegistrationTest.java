package airhacks.zsmith.tools.control;

import airhacks.zsmith.agent.boundary.Agent;

/**
 * Integration test verifying LinkCheckerTool can be registered with an Agent.
 * Validates: Requirements 5.1, 5.2
 */
public interface LinkCheckerToolRegistrationTest {

    static void main(String... args) {
        testToolRegistrationWithAgent();
        testToolRegistrationAlongsideOtherTools();
        System.out.println("All LinkCheckerTool registration tests passed.");
    }

    /** Req 5.1, 5.2: LinkCheckerTool can be registered via withTool() */
    static void testToolRegistrationWithAgent() {
        var agent = new Agent().withSystemPrompt("You are a helpful assistant.")
                .withTool(new LinkCheckerTool());

        assert agent != null : "Agent with LinkCheckerTool should be created";
        assert agent.tools().containsKey("check_link") : "Agent should contain 'check_link' tool";
    }

    /** Req 5.1: LinkCheckerTool can be registered alongside other tools */
    static void testToolRegistrationAlongsideOtherTools() {
        var agent = new Agent().withSystemPrompt("You are a helpful assistant.")
                .withTool(new CalculatorTool())
                .withTool(new LinkCheckerTool());

        assert agent.tools().containsKey("check_link") : "Agent should contain 'check_link' tool";
        assert agent.tools().containsKey("calculator") : "Agent should still contain 'calculator' tool";
        assert agent.tools().size() >= 2 : "Agent should have at least 2 tools";
    }
}

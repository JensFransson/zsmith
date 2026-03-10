package airhacks.zsmith.tools.control;

import org.json.JSONObject;

/**
 * Unit tests for UserConfirmationTool — no test framework.
 * Validates: Requirements 1.1, 4.2, 4.3, 4.4, 5.1, 5.2, 3.2, 3.3, 3.4
 */
public interface UserConfirmationToolTest {

    static void main(String... args) {
        testNameReturnsUserConfirmation();
        testDescriptionIsNonEmpty();
        testInputSchemaContainsQuestionAndRequired();
        testMissingQuestionThrowsIllegalArgument();
        testEmptyQuestionThrowsIllegalArgument();
        testAffirmativeYesReturnsYes();
        testAffirmativeYReturnsYes();
        testNonAffirmativeReturnsNo();
        testExplicitNoReturnsNo();
        System.out.println("All UserConfirmationTool unit tests passed.");
    }

    /** Req 4.2: Tool name is "user_confirmation" */
    static void testNameReturnsUserConfirmation() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        assert "user_confirmation".equals(tool.name()) : "name() should return 'user_confirmation', got: " + tool.name();
    }

    /** Req 4.3: Description is non-empty */
    static void testDescriptionIsNonEmpty() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        var description = tool.description();
        assert description != null && !description.isBlank() : "description() should be non-empty";
    }

    /** Req 1.1, 4.4: Input schema requires a "question" string parameter */
    static void testInputSchemaContainsQuestionAndRequired() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        var schema = tool.inputSchema();
        assert schema.contains("\"question\"") : "inputSchema() should contain '\"question\"'";
        assert schema.contains("\"required\"") : "inputSchema() should contain '\"required\"'";
    }

    /** Req 5.1: Missing question throws IllegalArgumentException */
    static void testMissingQuestionThrowsIllegalArgument() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        try {
            tool.execute(new JSONObject());
            assert false : "Expected IllegalArgumentException for missing question";
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /** Req 5.2: Empty question throws IllegalArgumentException */
    static void testEmptyQuestionThrowsIllegalArgument() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        try {
            tool.execute(new JSONObject().put("question", ""));
            assert false : "Expected IllegalArgumentException for empty question";
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /** Req 3.2: "yes" input returns "yes" */
    static void testAffirmativeYesReturnsYes() {
        var tool = new UserConfirmationTool(prompt -> "yes");
        var result = tool.execute(new JSONObject().put("question", "Proceed?"));
        assert "yes".equals(result) : "Expected 'yes', got: " + result;
    }

    /** Req 3.2: "y" input returns "yes" */
    static void testAffirmativeYReturnsYes() {
        var tool = new UserConfirmationTool(prompt -> "y");
        var result = tool.execute(new JSONObject().put("question", "Proceed?"));
        assert "yes".equals(result) : "Expected 'yes', got: " + result;
    }

    /** Req 3.3, 3.4: Non-affirmative input returns "no" */
    static void testNonAffirmativeReturnsNo() {
        var tool = new UserConfirmationTool(prompt -> "maybe");
        var result = tool.execute(new JSONObject().put("question", "Proceed?"));
        assert "no".equals(result) : "Expected 'no', got: " + result;
    }

    /** Req 3.3: Explicit "no" input returns "no" */
    static void testExplicitNoReturnsNo() {
        var tool = new UserConfirmationTool(prompt -> "no");
        var result = tool.execute(new JSONObject().put("question", "Proceed?"));
        assert "no".equals(result) : "Expected 'no', got: " + result;
    }
}

package airhacks.zsmith.tools.control;

import org.json.JSONObject;

/**
 * Unit tests for LinkCheckerTool — no network calls, no test framework.
 * Validates: Requirements 1.3, 2.1, 2.2, 5.2, 5.3, 5.4
 */
public interface LinkCheckerToolTest {

    static void main(String... args) {
        testNoArgConstructor();
        testNameReturnsCheckLink();
        testDescriptionIsNonEmpty();
        testInputSchemaContainsUrlAndRequired();
        testMissingUrlReturnsError();
        testMalformedUrlReturnsError();
        System.out.println("All LinkCheckerTool unit tests passed.");
    }

    /** Req 5.4: No-arg constructor creates a valid instance */
    static void testNoArgConstructor() {
        var tool = new LinkCheckerTool();
        assert tool != null : "No-arg constructor should create a non-null instance";
    }

    /** Req 5.2: Tool name is "check_link" */
    static void testNameReturnsCheckLink() {
        var tool = new LinkCheckerTool();
        assert "check_link".equals(tool.name()) : "name() should return 'check_link', got: " + tool.name();
    }

    /** Req 5.3: Description is non-empty */
    static void testDescriptionIsNonEmpty() {
        var tool = new LinkCheckerTool();
        var description = tool.description();
        assert description != null && !description.isBlank() : "description() should be non-empty";
    }

    /** Req 1.3: Input schema requires a "url" string parameter */
    static void testInputSchemaContainsUrlAndRequired() {
        var tool = new LinkCheckerTool();
        var schema = tool.inputSchema();
        assert schema.contains("\"url\"") : "inputSchema() should contain '\"url\"'";
        assert schema.contains("\"required\"") : "inputSchema() should contain '\"required\"'";
    }

    /** Req 2.1: Missing url parameter returns appropriate error */
    static void testMissingUrlReturnsError() {
        var tool = new LinkCheckerTool();
        var result = tool.execute(new JSONObject());
        assert "Error: Missing required parameter: url".equals(result) : "Missing url should return error, got: " + result;
    }

    /** Req 2.2: Malformed URL returns "Error: Invalid URL" */
    static void testMalformedUrlReturnsError() {
        var tool = new LinkCheckerTool();
        var result = tool.execute(new JSONObject().put("url", "not a valid url"));
        assert result.startsWith("Error:") : "Malformed URL should return error, got: " + result;

        var result2 = tool.execute(new JSONObject().put("url", "missing-scheme"));
        assert "Error: Invalid URL".equals(result2) : "URL without scheme should return 'Error: Invalid URL', got: " + result2;
    }
}

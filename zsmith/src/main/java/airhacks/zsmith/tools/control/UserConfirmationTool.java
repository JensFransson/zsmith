package airhacks.zsmith.tools.control;

import java.util.function.Function;

import org.json.JSONObject;

public class UserConfirmationTool implements Tool {

    private final Function<String, String> promptFunction;

    public UserConfirmationTool() {
        this(IO::readln);
    }

    public UserConfirmationTool(Function<String, String> promptFunction) {
        this.promptFunction = promptFunction;
    }

    @Override
    public String name() {
        return "user_confirmation";
    }

    @Override
    public String description() {
        return "Asks the user a yes/no question and returns the answer";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "question": {
                            "type": "string",
                            "description": "The yes/no question to ask the user"
                        }
                    },
                    "required": ["question"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has("question") || input.getString("question").isEmpty()) {
            throw new IllegalArgumentException("Missing or empty required parameter: question");
        }
        var question = input.getString("question");
        var prompt = question + " (yes/no): ";
        var response = promptFunction.apply(prompt);
        return switch (response.toLowerCase()) {
            case "yes", "y" -> "yes";
            default -> "no";
        };
    }
}

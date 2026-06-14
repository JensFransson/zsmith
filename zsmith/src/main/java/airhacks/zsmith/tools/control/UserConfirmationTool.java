package airhacks.zsmith.tools.control;

import java.util.function.Function;

import org.json.JSONObject;

public class UserConfirmationTool implements Tool {

    private final Function<String, String> promptFunction;

    public UserConfirmationTool() {
        this(Console::prompt);
    }

    public UserConfirmationTool(Function<String, String> promptFunction) {
        this.promptFunction = promptFunction;
    }

    @Override
    public String toolName() {
        return "user_confirmation";
    }

    @Override
    public String description() {
        return "Asks the user a yes/no question and returns the answer";
    }

    enum Field { question }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(Prop.string(Field.question, "The yes/no question to ask the user"));
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has(Field.question.name()) || input.getString(Field.question.name()).isEmpty()) {
            throw new IllegalArgumentException("Missing or empty required parameter: question");
        }
        var question = input.getString(Field.question.name());
        var prompt = question + " (yes/no): ";
        var response = promptFunction.apply(prompt);
        return switch (response.toLowerCase()) {
            case "yes", "y" -> "yes";
            default -> "no";
        };
    }
}

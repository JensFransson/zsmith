package airhacks.zsmith.tools.control;

import java.util.function.Function;

import org.json.JSONObject;

public class UserQuestionTool implements Tool {

    private final Function<String, String> promptFunction;

    public UserQuestionTool() {
        this(Console::prompt);
    }

    public UserQuestionTool(Function<String, String> promptFunction) {
        this.promptFunction = promptFunction;
    }

    @Override
    public String toolName() {
        return "user_question";
    }

    @Override
    public String description() {
        return "Asks the user a question and returns the typed answer";
    }

    enum Field { question }

    @Override
    public String inputSchema() {
        return Tool.schema(Prop.string(Field.question, "The question to ask the user"));
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has(Field.question.name()) || input.getString(Field.question.name()).isEmpty()) {
            throw new IllegalArgumentException("Missing or empty required parameter: question");
        }
        var question = input.getString(Field.question.name());
        var prompt = question + ": ";
        return promptFunction.apply(prompt);
    }
}

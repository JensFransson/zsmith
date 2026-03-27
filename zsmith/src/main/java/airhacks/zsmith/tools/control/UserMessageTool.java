package airhacks.zsmith.tools.control;

import java.util.function.Consumer;

import org.json.JSONObject;

public class UserMessageTool implements Tool {

    private final Consumer<String> messageConsumer;

    public UserMessageTool() {
        this(System.out::println);
    }

    public UserMessageTool(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public String toolName() {
        return "user_message";
    }

    @Override
    public String description() {
        return "Presents a message to the user. Use this to display important information, status updates, or notifications.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "message": {
                            "type": "string",
                            "description": "The message to present to the user"
                        }
                    },
                    "required": ["message"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has("message") || input.getString("message").isEmpty()) {
            return "Error: Missing or empty required parameter: message";
        }
        var message = input.getString("message");
        messageConsumer.accept(message);
        return "Message presented to user";
    }
}

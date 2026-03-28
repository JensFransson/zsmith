package airhacks.zsmith.tools.control;

import java.util.function.Consumer;

import org.json.JSONObject;

import airhacks.zsmith.logging.control.Log;

public class UserMessageTool implements Tool {

    private final Consumer<String> messageConsumer;

    public UserMessageTool() {
        this(Log::user);
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
        return Tool.schema(Prop.string("message", "The message to present to the user"));
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

package airhacks.zsmith;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tools.boundary.Tools;

public interface UserConfirmationExample {
    static void main(String...args) {
        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with access to tools.
                        Use the user_confirmation tool to ask the user yes/no questions before proceeding with actions.
                        Be concise in your responses.
                        """)
                .withTool(Tools.USER_CONFIRMATION);

        Log.agent("Agent initialized with user_confirmation tool");

        var question = "I want to create a HelloWorld.java example. Can you help?";
        Log.prompt("User: " + question);

        var response = agent.chat(question);
        Log.answer("Agent: " + response);
    }
}

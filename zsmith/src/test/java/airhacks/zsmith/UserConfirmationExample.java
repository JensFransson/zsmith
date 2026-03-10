package airhacks.zsmith;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tools.control.UserConfirmationTool;

public interface UserConfirmationExample {
    static void main(String...args) {
        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with access to tools.
                        Use the user_confirmation tool to ask the user yes/no questions before proceeding with actions.
                        Be concise in your responses.
                        """)
                .withTool(new UserConfirmationTool());

        Log.INFO.out("Agent initialized with user_confirmation tool");

        var question = "I want to create a HelloWorld.java example. Can you help?";
        Log.PROMPT.out("User: " + question);

        var response = agent.chat(question);
        Log.ANSWER.out("Agent: " + response);
    }
}

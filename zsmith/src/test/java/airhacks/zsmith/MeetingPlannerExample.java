package airhacks.zsmith;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tools.boundary.Tools;

public interface MeetingPlannerExample {
    static void main(String...args) {
        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with access to tools.
                        Use the calculator tool for math operations.
                        Use the current_time tool to get the current date and time.
                        Be concise in your responses.
                        """)
                .withTools(Tools.CALCULATOR, Tools.CURRENT_TIME);

        Log.agent("Agent initialized with calculator and current_time tools");

        var question = "I have a meeting in 90 minutes. What time does it start? If the meeting lasts 45 minutes, what time does it end?";
        Log.prompt("User: " + question);

        var response = agent.chat(question);
        Log.answer("Agent: " + response);
    }
}

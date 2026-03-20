package airhacks.zsmith;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;

public interface SkillsExample {
    static void main(String...args) {
        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with access to skills.
                        When the user's request matches an available skill, load it first, then follow its instructions.
                        Be concise in your responses.
                        """)
                .withSkills();

        Log.agent("Agent initialized with skills");

        var question = "Explain what a record is in Java";
        Log.prompt("User: " + question);

        var response = agent.chat(question);
        Log.answer("Agent: " + response);
    }
}

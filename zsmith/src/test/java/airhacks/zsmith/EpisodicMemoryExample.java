package airhacks.zsmith;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;

public interface EpisodicMemoryExample {
    static void main(String...args) {
        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with long-term memory.
                        Use the store_memory tool to remember important information about the user and project.
                        Use the recall_memory tool to retrieve previously stored memories.
                        Always check your memory before answering questions about past interactions.
                        Be concise in your responses.
                        """)
                .withEpisodicMemory();

        Log.info("Agent initialized with episodic memory tools");

        var question = "Remember that I prefer Java 25 and work on microservices. My name is Duke.";
        Log.prompt("User: " + question);

        var response = agent.chat(question);
        Log.answer("Agent: " + response);

        var followUp = "What do you know about me?";
        Log.prompt("User: " + followUp);

        var recallResponse = agent.chat(followUp);
        Log.answer("Agent: " + recallResponse);
    }
}

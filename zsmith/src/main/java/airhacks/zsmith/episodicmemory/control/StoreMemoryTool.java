package airhacks.zsmith.episodicmemory.control;

import org.json.JSONObject;

import airhacks.zsmith.episodicmemory.boundary.EpisodicMemoryStore;
import airhacks.zsmith.episodicmemory.entity.Episode;
import airhacks.zsmith.tools.control.Tool;

public class StoreMemoryTool implements Tool {

    private final EpisodicMemoryStore store;

    public StoreMemoryTool(EpisodicMemoryStore store) {
        this.store = store;
    }

    @Override
    public String name() {
        return "store_memory";
    }

    @Override
    public String description() {
        return "Stores an episode in long-term memory for future recall. Use this to remember important facts, decisions, or outcomes.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "content": {
                            "type": "string",
                            "description": "The information to remember"
                        },
                        "category": {
                            "type": "string",
                            "description": "Optional category for organizing memories"
                        }
                    },
                    "required": ["content"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        var content = input.getString("content");
        var category = input.optString("category", null);
        var episode = new Episode(content, null, category);
        store.store(episode);
        return "Memory stored.";
    }
}

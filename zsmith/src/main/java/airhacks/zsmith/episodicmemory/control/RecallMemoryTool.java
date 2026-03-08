package airhacks.zsmith.episodicmemory.control;

import java.util.List;

import org.json.JSONObject;

import airhacks.zsmith.episodicmemory.boundary.EpisodicMemoryStore;
import airhacks.zsmith.episodicmemory.entity.Episode;
import airhacks.zsmith.tools.control.Tool;

public class RecallMemoryTool implements Tool {

    private final EpisodicMemoryStore store;

    public RecallMemoryTool(EpisodicMemoryStore store) {
        this.store = store;
    }

    @Override
    public String name() {
        return "recall_memory";
    }

    @Override
    public String description() {
        return "Recalls past memories. Optionally filter by category or limit to the most recent entries.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "category": {
                            "type": "string",
                            "description": "Optional category to filter memories"
                        },
                        "limit": {
                            "type": "integer",
                            "description": "Maximum number of recent memories to return. Defaults to 10."
                        }
                    }
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        var category = input.optString("category", null);
        var limit = input.optInt("limit", 10);

        List<Episode> episodes;
        if (category != null) {
            episodes = store.byCategory(category);
        } else {
            episodes = store.recent(limit);
        }

        if (episodes.isEmpty()) {
            return "No memories found.";
        }

        var result = new StringBuilder();
        for (var episode : episodes) {
            result.append("[%s] %s".formatted(episode.timestamp(), episode.content()));
            if (episode.category() != null) {
                result.append(" (category: %s)".formatted(episode.category()));
            }
            result.append("\n");
        }
        return result.toString().strip();
    }
}

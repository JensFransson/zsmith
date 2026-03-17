package airhacks.zsmith.skills.control;

import org.json.JSONObject;

import airhacks.zsmith.skills.boundary.SkillStore;
import airhacks.zsmith.tools.control.Tool;

public class LoadSkillTool implements Tool {

    private final SkillStore store;

    public LoadSkillTool(SkillStore store) {
        this.store = store;
    }

    @Override
    public String toolName() {
        return "load_skill";
    }

    @Override
    public String description() {
        return "Loads a skill by name. Returns the full skill content with instructions the assistant should follow to complete the user's request.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string",
                            "description": "The name of the skill to load"
                        }
                    },
                    "required": ["name"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        var name = input.getString("name");
        var skill = store.load(name);
        if (skill == null) {
            return "Skill not found: " + name;
        }
        return skill.content();
    }
}

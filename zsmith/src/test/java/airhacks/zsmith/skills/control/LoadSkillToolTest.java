package airhacks.zsmith.skills.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import airhacks.zsmith.skills.boundary.SkillStore;

import static org.junit.jupiter.api.Assertions.*;

class LoadSkillToolTest {

    LoadSkillTool tool;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        var skillDir = tempDir.resolve("greeting");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("skill.prompt"), """
                ---
                name: greeting
                description: Greets the user
                ---
                Always greet the user warmly and ask how you can help.
                """);
        var store = new SkillStore(List.of(tempDir));
        this.tool = new LoadSkillTool(store);
    }

    @Test
    void loadExistingSkill() {
        var input = new JSONObject().put("name", "greeting");
        var result = tool.execute(input);
        assertEquals("Always greet the user warmly and ask how you can help.", result);
    }

    @Test
    void loadNonExistentSkill() {
        var input = new JSONObject().put("name", "nonexistent");
        var result = tool.execute(input);
        assertTrue(result.contains("Skill not found"));
    }

    @Test
    void toolDefinition() {
        assertEquals("load_skill", tool.toolName());
        assertFalse(tool.description().isBlank());
        assertFalse(tool.inputSchema().isBlank());
    }
}

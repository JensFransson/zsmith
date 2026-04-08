import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;

import airhacks.zsmith.skills.boundary.SkillStore;
import airhacks.zsmith.skills.control.LoadSkillTool;

void main() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-loadskill");
    try {
        var skillDir = tempDir.resolve("greeting");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
                ---
                name: greeting
                description: Greets the user
                ---
                Always greet the user warmly and ask how you can help.
                """);
        var store = new SkillStore(List.of(tempDir));
        var tool = new LoadSkillTool(store);

        // load existing skill
        var result = tool.execute(new JSONObject().put("name", "greeting"));
        if (!"Always greet the user warmly and ask how you can help.".equals(result))
            throw new AssertionError("expected skill content but got: " + result);

        // load non-existent skill
        var missing = tool.execute(new JSONObject().put("name", "nonexistent"));
        if (!missing.contains("Skill not found"))
            throw new AssertionError("expected 'Skill not found' but got: " + missing);

        // tool definition
        if (!"load_skill".equals(tool.toolName()))
            throw new AssertionError("expected 'load_skill' but got: " + tool.toolName());
        if (tool.description().isBlank())
            throw new AssertionError("description should not be blank");
        if (tool.inputSchema().isBlank())
            throw new AssertionError("inputSchema should not be blank");
    } finally {
        try (var walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}

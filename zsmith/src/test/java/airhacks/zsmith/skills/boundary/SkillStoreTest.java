package airhacks.zsmith.skills.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class SkillStoreTest {

    @Test
    void parseSkillFileWithFrontmatter() {
        var raw = """
                ---
                name: rest-resource
                description: Creates a JAX-RS resource
                ---
                Generate a REST resource with proper BCE structure.
                """;
        var skill = SkillStore.parseSkillFile(raw, "fallback-name", Path.of("test"));
        assertEquals("rest-resource", skill.name());
        assertEquals("Creates a JAX-RS resource", skill.description());
        assertEquals("Generate a REST resource with proper BCE structure.", skill.content());
    }

    @Test
    void parseSkillFileWithoutFrontmatter() {
        var raw = "Just plain content here.";
        var skill = SkillStore.parseSkillFile(raw, "my-skill", Path.of("test"));
        assertEquals("my-skill", skill.name());
        assertEquals("Just plain content here.", skill.content());
    }

    @Test
    void parseSkillFileNameFallsBackToDirectoryName() {
        var raw = """
                ---
                description: A skill without a name field
                ---
                Some content.
                """;
        var skill = SkillStore.parseSkillFile(raw, "dir-name", Path.of("test"));
        assertEquals("dir-name", skill.name());
        assertEquals("A skill without a name field", skill.description());
    }

    @Test
    void scanDirectoryLoadsSkills(@TempDir Path tempDir) throws IOException {
        var skillDir = tempDir.resolve("my-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("skill.prompt"), """
                ---
                name: my-skill
                description: Does something
                ---
                Full instructions here.
                """);

        var store = new SkillStore(List.of(tempDir));
        var skills = store.allSkills();
        assertEquals(1, skills.size());
        assertEquals("my-skill", skills.getFirst().name());
    }

    @Test
    void laterDirectoryOverridesEarlier(@TempDir Path tempDir) throws IOException {
        var dir1 = tempDir.resolve("dir1");
        var dir2 = tempDir.resolve("dir2");
        var skill1 = dir1.resolve("overlap");
        var skill2 = dir2.resolve("overlap");
        Files.createDirectories(skill1);
        Files.createDirectories(skill2);
        Files.writeString(skill1.resolve("skill.prompt"), """
                ---
                name: overlap
                description: First version
                ---
                Content from dir1.
                """);
        Files.writeString(skill2.resolve("skill.prompt"), """
                ---
                name: overlap
                description: Second version
                ---
                Content from dir2.
                """);

        var store = new SkillStore(List.of(dir1, dir2));
        var skill = store.load("overlap");
        assertEquals("Second version", skill.description());
        assertEquals("Content from dir2.", skill.content());
    }

    @Test
    void catalogFormatsCorrectly(@TempDir Path tempDir) throws IOException {
        var skillDir = tempDir.resolve("test-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("skill.prompt"), """
                ---
                name: test-skill
                description: A test skill
                ---
                Content.
                """);

        var store = new SkillStore(List.of(tempDir));
        var catalog = store.catalog();
        assertTrue(catalog.contains("## Available Skills"));
        assertTrue(catalog.contains("- test-skill: A test skill"));
        assertTrue(catalog.contains("load_skill"));
    }

    @Test
    void emptyDirectoryProducesEmptyCatalog(@TempDir Path tempDir) {
        var store = new SkillStore(List.of(tempDir));
        assertEquals("", store.catalog());
    }

    @Test
    void nonExistentDirectoryIsIgnored() {
        var store = new SkillStore(List.of(Path.of("/nonexistent/path")));
        assertTrue(store.allSkills().isEmpty());
    }

    @Test
    void loadReturnsNullForUnknownSkill(@TempDir Path tempDir) {
        var store = new SkillStore(List.of(tempDir));
        assertNull(store.load("nonexistent"));
    }
}

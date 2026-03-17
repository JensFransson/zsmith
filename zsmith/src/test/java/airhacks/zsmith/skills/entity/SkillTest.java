package airhacks.zsmith.skills.entity;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillTest {

    @Test
    void createSkill() {
        var skill = new Skill("rest-resource", "Creates a JAX-RS resource", "Generate a REST resource...", Path.of("skills/rest-resource/skill.prompt"));
        assertEquals("rest-resource", skill.name());
        assertEquals("Creates a JAX-RS resource", skill.description());
        assertEquals("Generate a REST resource...", skill.content());
    }

    @Test
    void descriptionFallsBackToFirstLine() {
        var skill = new Skill("test", null, "First line of content\nSecond line", Path.of("test"));
        assertEquals("First line of content", skill.description());
    }

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Skill("", "desc", "content", Path.of("test")));
    }

    @Test
    void blankContentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Skill("test", "desc", "", Path.of("test")));
    }

    @Test
    void catalogEntry() {
        var skill = new Skill("deploy", "Deploy the application", "content here", Path.of("test"));
        assertEquals("- deploy: Deploy the application", skill.catalogEntry());
    }
}

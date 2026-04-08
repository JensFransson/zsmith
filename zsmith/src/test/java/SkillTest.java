import java.nio.file.Path;

import airhacks.zsmith.skills.entity.Skill;

void main() {
    // create skill with all fields
    var skill = new Skill("rest-resource", "Creates a JAX-RS resource", "Generate a REST resource...", Path.of("skills/rest-resource/SKILL.md"));
    if (!"rest-resource".equals(skill.name()))
        throw new AssertionError("expected 'rest-resource' but got " + skill.name());
    if (!"Creates a JAX-RS resource".equals(skill.description()))
        throw new AssertionError("expected 'Creates a JAX-RS resource' but got " + skill.description());
    if (!"Generate a REST resource...".equals(skill.content()))
        throw new AssertionError("expected 'Generate a REST resource...' but got " + skill.content());

    // description falls back to first line of content
    var fallback = new Skill("test", null, "First line of content\nSecond line", Path.of("test"));
    if (!"First line of content".equals(fallback.description()))
        throw new AssertionError("expected 'First line of content' but got " + fallback.description());

    // blank name throws
    try {
        new Skill("", "desc", "content", Path.of("test"));
        throw new AssertionError("expected IllegalArgumentException for blank name");
    } catch (IllegalArgumentException expected) {
    }

    // blank content throws
    try {
        new Skill("test", "desc", "", Path.of("test"));
        throw new AssertionError("expected IllegalArgumentException for blank content");
    } catch (IllegalArgumentException expected) {
    }

    // catalog entry format
    var deploy = new Skill("deploy", "Deploy the application", "content here", Path.of("test"));
    var entry = deploy.catalogEntry();
    if (!"- deploy: Deploy the application".equals(entry))
        throw new AssertionError("expected '- deploy: Deploy the application' but got " + entry);
}

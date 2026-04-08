import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import airhacks.zsmith.skills.boundary.SkillStore;

void main() throws IOException {
    loadSkillWithFrontmatter();
    loadSkillWithoutFrontmatter();
    loadSkillNameFallsBackToDirectoryName();
    laterDirectoryOverridesEarlier();
    catalogFormatsCorrectly();
    emptyDirectoryProducesEmptyCatalog();
    nonExistentDirectoryIsIgnored();
    loadReturnsNullForUnknownSkill();
}

void loadSkillWithFrontmatter() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var skillDir = tempDir.resolve("rest-resource");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
                ---
                name: rest-resource
                description: Creates a JAX-RS resource
                ---
                Generate a REST resource with proper BCE structure.
                """);

        var store = new SkillStore(List.of(tempDir));
        var skill = store.load("rest-resource");
        if (skill == null)
            throw new AssertionError("skill should not be null");
        if (!"rest-resource".equals(skill.name()))
            throw new AssertionError("expected 'rest-resource' but got " + skill.name());
        if (!"Creates a JAX-RS resource".equals(skill.description()))
            throw new AssertionError("expected 'Creates a JAX-RS resource' but got " + skill.description());
        if (!"Generate a REST resource with proper BCE structure.".equals(skill.content()))
            throw new AssertionError("expected content mismatch, got: " + skill.content());
    } finally {
        deleteRecursively(tempDir);
    }
}

void loadSkillWithoutFrontmatter() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var skillDir = tempDir.resolve("my-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), "Just plain content here.");

        var store = new SkillStore(List.of(tempDir));
        var skill = store.load("my-skill");
        if (skill == null)
            throw new AssertionError("skill should not be null");
        if (!"my-skill".equals(skill.name()))
            throw new AssertionError("expected 'my-skill' but got " + skill.name());
        if (!"Just plain content here.".equals(skill.content()))
            throw new AssertionError("expected 'Just plain content here.' but got " + skill.content());
    } finally {
        deleteRecursively(tempDir);
    }
}

void loadSkillNameFallsBackToDirectoryName() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var skillDir = tempDir.resolve("dir-name");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
                ---
                description: A skill without a name field
                ---
                Some content.
                """);

        var store = new SkillStore(List.of(tempDir));
        var skill = store.load("dir-name");
        if (skill == null)
            throw new AssertionError("skill should not be null");
        if (!"dir-name".equals(skill.name()))
            throw new AssertionError("expected 'dir-name' but got " + skill.name());
        if (!"A skill without a name field".equals(skill.description()))
            throw new AssertionError("expected 'A skill without a name field' but got " + skill.description());
    } finally {
        deleteRecursively(tempDir);
    }
}

void laterDirectoryOverridesEarlier() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var dir1 = tempDir.resolve("dir1");
        var dir2 = tempDir.resolve("dir2");
        var skill1 = dir1.resolve("overlap");
        var skill2 = dir2.resolve("overlap");
        Files.createDirectories(skill1);
        Files.createDirectories(skill2);
        Files.writeString(skill1.resolve("SKILL.md"), """
                ---
                name: overlap
                description: First version
                ---
                Content from dir1.
                """);
        Files.writeString(skill2.resolve("SKILL.md"), """
                ---
                name: overlap
                description: Second version
                ---
                Content from dir2.
                """);

        var store = new SkillStore(List.of(dir1, dir2));
        var skill = store.load("overlap");
        if (!"Second version".equals(skill.description()))
            throw new AssertionError("expected 'Second version' but got " + skill.description());
        if (!"Content from dir2.".equals(skill.content()))
            throw new AssertionError("expected 'Content from dir2.' but got " + skill.content());
    } finally {
        deleteRecursively(tempDir);
    }
}

void catalogFormatsCorrectly() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var skillDir = tempDir.resolve("test-skill");
        Files.createDirectories(skillDir);
        Files.writeString(skillDir.resolve("SKILL.md"), """
                ---
                name: test-skill
                description: A test skill
                ---
                Content.
                """);

        var store = new SkillStore(List.of(tempDir));
        var catalog = store.catalog();
        if (!catalog.contains("## Available Skills"))
            throw new AssertionError("catalog should contain '## Available Skills'");
        if (!catalog.contains("- test-skill: A test skill"))
            throw new AssertionError("catalog should contain '- test-skill: A test skill'");
        if (!catalog.contains("load_skill"))
            throw new AssertionError("catalog should contain 'load_skill'");
    } finally {
        deleteRecursively(tempDir);
    }
}

void emptyDirectoryProducesEmptyCatalog() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var store = new SkillStore(List.of(tempDir));
        if (!"".equals(store.catalog()))
            throw new AssertionError("expected empty catalog but got: " + store.catalog());
    } finally {
        deleteRecursively(tempDir);
    }
}

void nonExistentDirectoryIsIgnored() {
    var store = new SkillStore(List.of(Path.of("/nonexistent/path")));
    if (!store.allSkills().isEmpty())
        throw new AssertionError("expected empty skills for non-existent directory");
}

void loadReturnsNullForUnknownSkill() throws IOException {
    var tempDir = Files.createTempDirectory("zunit-skillstore");
    try {
        var store = new SkillStore(List.of(tempDir));
        if (store.load("nonexistent") != null)
            throw new AssertionError("expected null for unknown skill");
    } finally {
        deleteRecursively(tempDir);
    }
}

static void deleteRecursively(Path path) throws IOException {
    try (var walk = Files.walk(path)) {
        walk.sorted(Comparator.reverseOrder())
            .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
    }
}

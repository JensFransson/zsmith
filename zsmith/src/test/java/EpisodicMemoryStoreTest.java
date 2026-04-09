import java.nio.file.Files;
import java.nio.file.Path;

import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.episodicmemory.boundary.EpisodicMemoryStore;
import airhacks.zsmith.episodicmemory.entity.Episode;
import airhacks.zsmith.episodicmemory.entity.MemoryType;

void main() throws Exception {
    ZCfg.loadBaseConfig("zsmith-test-" + ProcessHandle.current().pid());

    var tempFile = Files.createTempFile("episodic-test", ".json");
    Files.deleteIfExists(tempFile);

    var store = new EpisodicMemoryStore(tempFile);

    // store episodes of different types
    store.store(Episode.of("user pref", MemoryType.user));
    store.store(Episode.of("project note", MemoryType.project));
    store.store(Episode.of("another user pref", MemoryType.user));
    store.store(Episode.of("feedback item", MemoryType.feedback));

    // allEpisodes returns all
    assert store.allEpisodes().size() == 4 : "expected 4 episodes, got: " + store.allEpisodes().size();

    // byType filters correctly
    var userEpisodes = store.byType(MemoryType.user);
    assert userEpisodes.size() == 2 : "expected 2 user episodes, got: " + userEpisodes.size();
    assert userEpisodes.stream().allMatch(e -> e.type() == MemoryType.user) : "all should be user type";

    var projectEpisodes = store.byType(MemoryType.project);
    assert projectEpisodes.size() == 1 : "expected 1 project episode, got: " + projectEpisodes.size();

    // recent(n) returns last n
    var recent2 = store.recent(2);
    assert recent2.size() == 2 : "expected 2 recent, got: " + recent2.size();
    assert "feedback item".equals(recent2.getLast().content()) : "last recent should be feedback item";

    // recent(0) returns empty
    assert store.recent(0).isEmpty() : "recent(0) should be empty";

    // clear removes all
    store.clear();
    assert store.allEpisodes().isEmpty() : "should be empty after clear";
    assert !Files.exists(tempFile) : "file should be deleted after clear";
}

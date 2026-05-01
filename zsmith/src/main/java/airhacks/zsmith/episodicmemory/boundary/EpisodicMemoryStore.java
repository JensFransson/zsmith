package airhacks.zsmith.episodicmemory.boundary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;

import airhacks.zsmith.episodicmemory.entity.Episode;
import airhacks.zsmith.episodicmemory.entity.MemoryAccessEvent;
import airhacks.zsmith.episodicmemory.entity.MemoryType;
import airhacks.zsmith.configuration.control.ZCfg;
import airhacks.zsmith.logging.control.Log;

public class EpisodicMemoryStore {

    private final List<Episode> episodes;
    private final Path filePath;

    public EpisodicMemoryStore(Path filePath) {
        this.filePath = filePath;
        this.episodes = new ArrayList<>();
        load();
    }

    public EpisodicMemoryStore() {
        this(defaultPath());
    }

    static Path defaultPath() {
        return resolvePath("memory");
    }

    public static Path agentPath(String agentName) {
        return resolvePath(agentName + "/memory");
    }

    private static Path resolvePath(String subdir) {
        var userHome = System.getProperty("user.home");
        var memoryDir = Path.of(userHome, "." + ZCfg.APP_NAME, subdir);
        try {
            Files.createDirectories(memoryDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return memoryDir.resolve("episodic-memory.json");
    }

    public void store(Episode episode) {
        this.episodes.add(episode);
        save();
    }

    public List<Episode> allEpisodes() {
        return this.episodes.stream()
                .sorted(Comparator.comparing(Episode::timestamp))
                .toList();
    }

    public List<Episode> byType(MemoryType type) {
        return this.episodes.stream()
                .filter(e -> e.hasType(type))
                .sorted(Comparator.comparing(Episode::timestamp))
                .toList();
    }

    public List<Episode> recent(int n) {
        if (n <= 0) {
            return List.of();
        }
        var sorted = this.episodes.stream()
                .sorted(Comparator.comparing(Episode::timestamp))
                .toList();
        var fromIndex = Math.max(0, sorted.size() - n);
        return List.copyOf(sorted.subList(fromIndex, sorted.size()));
    }

    public void clear() {
        this.episodes.clear();
        try {
            Files.deleteIfExists(this.filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void save() {
        var event = new MemoryAccessEvent();
        event.store = "episodic";
        event.operation = "save";
        event.episodeCount = this.episodes.size();
        event.begin();
        try {
            var array = new JSONArray();
            this.episodes.stream()
                    .map(Episode::toJSON)
                    .forEach(array::put);
            var payload = array.toString();
            event.payloadSize = payload.length();
            try {
                Files.writeString(this.filePath, payload);
                event.outcome = "success";
            } catch (IOException e) {
                event.outcome = "io_error";
                throw new UncheckedIOException(e);
            }
        } finally {
            if (event.shouldCommit()) {
                event.commit();
            }
        }
    }

    void load() {
        if (!Files.exists(this.filePath)) {
            return;
        }
        var event = new MemoryAccessEvent();
        event.store = "episodic";
        event.operation = "load";
        event.begin();
        try {
            var json = Files.readString(this.filePath);
            event.payloadSize = json.length();
            var array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                var episode = Episode.fromJSON(array.getJSONObject(i));
                this.episodes.add(episode);
            }
            event.episodeCount = this.episodes.size();
            event.outcome = "success";
        } catch (IOException e) {
            Log.warning("could not load episodic memory from " + this.filePath + ": " + e.getMessage());
            event.outcome = "io_error";
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.warning("malformed JSON in " + this.filePath + ": " + e.getMessage());
            event.outcome = "parse_error";
        } finally {
            if (event.shouldCommit()) {
                event.commit();
            }
        }
    }
}

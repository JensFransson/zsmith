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
        this(Path.of("episodic-memory.json"));
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

    public List<Episode> byCategory(String category) {
        return this.episodes.stream()
                .filter(e -> Objects.equals(e.category(), category))
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
        var array = new JSONArray();
        this.episodes.stream()
                .map(Episode::toJSON)
                .forEach(array::put);
        try {
            Files.writeString(this.filePath, array.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void load() {
        if (!Files.exists(this.filePath)) {
            return;
        }
        try {
            var json = Files.readString(this.filePath);
            var array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                this.episodes.add(Episode.fromJSON(array.getJSONObject(i)));
            }
        } catch (IOException e) {
            Log.error("Warning: could not load episodic memory from " + this.filePath + ": " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.error("Warning: malformed JSON in " + this.filePath + ": " + e.getMessage());
        }
    }
}

package airhacks.zsmith.episodicmemory.entity;

import java.time.Instant;

import org.json.JSONObject;

public record Episode(String content, String timestamp, String category) {

    public Episode {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Episode content must not be empty");
        }
        if (timestamp == null) {
            timestamp = Instant.now().toString();
        }
    }

    public static Episode of(String content) {
        return new Episode(content, null, null);
    }

    public static Episode of(String content, String category) {
        return new Episode(content, null, category);
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("content", this.content)
                .put("timestamp", this.timestamp)
                .put("category", this.category == null ? JSONObject.NULL : this.category);
    }

    public static Episode fromJSON(JSONObject json) {
        var content = json.getString("content");
        var timestamp = json.getString("timestamp");
        var category = json.isNull("category") ? null : json.getString("category");
        return new Episode(content, timestamp, category);
    }
}

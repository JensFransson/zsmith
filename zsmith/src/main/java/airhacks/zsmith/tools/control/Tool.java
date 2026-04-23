package airhacks.zsmith.tools.control;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public interface Tool {

    String toolName();

    String description();

    String inputSchema();

    String execute(JSONObject input);

    default boolean parallel() { return false; }

    default JSONObject toToolDefinition() {
        return new JSONObject()
                .put("name", toolName())
                .put("description", description())
                .put("input_schema", new JSONObject(inputSchema()));
    }

    record Prop<E extends Enum<E>>(E name, String type, String description, List<String> enumValues, boolean required) {

        public static <E extends Enum<E>> Prop<E> string(E name, String description) {
            return new Prop<>(name, "string", description, List.of(), true);
        }

        public static <E extends Enum<E>> Prop<E> stringEnum(E name, String description, String... values) {
            return new Prop<>(name, "string", description, List.of(values), true);
        }

        public static <E extends Enum<E>> Prop<E> number(E name, String description) {
            return new Prop<>(name, "number", description, List.of(), true);
        }

        public static <E extends Enum<E>> Prop<E> integer(E name, String description) {
            return new Prop<>(name, "integer", description, List.of(), true);
        }

        public Prop<E> optional() {
            return new Prop<>(name, type, description, enumValues, false);
        }
    }

    static String emptySchema() {
        return """
                {"type":"object","properties":{}}""";
    }

    static String schema(Prop<?>... props) {
        var properties = new JSONObject();
        var required = new JSONArray();
        for (var prop : props) {
            var p = new JSONObject()
                    .put("type", prop.type())
                    .put("description", prop.description());
            if (!prop.enumValues().isEmpty()) {
                p.put("enum", new JSONArray(prop.enumValues()));
            }
            properties.put(prop.name().name(), p);
            if (prop.required()) {
                required.put(prop.name().name());
            }
        }
        var schema = new JSONObject()
                .put("type", "object")
                .put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema.toString();
    }
}

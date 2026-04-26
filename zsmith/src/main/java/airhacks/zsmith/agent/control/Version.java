package airhacks.zsmith.agent.control;

import java.nio.file.Files;
import java.nio.file.Path;

public class Version {

    static final String VERSION_FILE = "/version.txt";
    static final Path[] FALLBACK_PATHS = {
            Path.of("src/main/resources/version.txt"),
            Path.of("zsmith/src/main/resources/version.txt")
    };

    public static String current() {
        try (var in = Version.class.getResourceAsStream(VERSION_FILE)) {
            if (in != null) {
                return new String(in.readAllBytes()).strip();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + VERSION_FILE + " from classpath", e);
        }
        return fromFilesystem();
    }

    static String fromFilesystem() {
        for (var path : FALLBACK_PATHS) {
            if (Files.exists(path)) {
                try {
                    return Files.readString(path).strip();
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot read " + path, e);
                }
            }
        }
        throw new IllegalStateException("version.txt not found on classpath or in " + java.util.Arrays.toString(FALLBACK_PATHS));
    }
}

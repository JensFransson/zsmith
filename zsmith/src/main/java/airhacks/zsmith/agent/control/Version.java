package airhacks.zsmith.agent.control;

public class Version {

    static final String VERSION_FILE = "/version.txt";

    public static String current() {
        try (var in = Version.class.getResourceAsStream(VERSION_FILE)) {
            return new String(in.readAllBytes()).strip();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + VERSION_FILE + " from classpath", e);
        }
    }
}

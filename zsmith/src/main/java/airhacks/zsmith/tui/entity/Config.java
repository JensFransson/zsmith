package airhacks.zsmith.tui.entity;

public record Config(String host, int port, String sessionId, int timeout) {

    public static Config parse(String[] args) {
        var host = "localhost";
        var port = 8080;
        String sessionId = null;
        var timeout = 120;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--session" -> sessionId = args[++i];
                case "--timeout" -> timeout = Integer.parseInt(args[++i]);
                default -> {
                    System.out.println("Usage: zsmith-chat [--host HOST] [--port PORT] [--session ID] [--timeout SECS]");
                    System.exit(1);
                }
            }
        }
        return new Config(host, port, sessionId, timeout);
    }
}

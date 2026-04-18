package airhacks.zsmith.tui.boundary;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tui.control.ChatClient;
import airhacks.zsmith.tui.entity.Config;

public class Chat {

    final ChatClient client;
    final Config config;
    String sessionId;

    public Chat(Config config) {
        this.config = config;
        this.client = new ChatClient(config);
        this.sessionId = config.sessionId();
    }

    public void start() {
        banner();

        var reader = System.console() != null
                ? System.console().reader()
                : new BufferedReader(new InputStreamReader(System.in));

        try (reader) {
            String line;
            while (true) {
                System.out.print("> ");
                System.out.flush();
                line = reader instanceof BufferedReader br ? br.readLine() : System.console().readLine();
                if (line == null) break;
                line = line.strip();
                if (line.isEmpty()) continue;

                if (line.startsWith("/")) {
                    if (line.equals("/quit") || line.equals("/exit")) break;
                    handleCommand(line);
                    continue;
                }

                var response = this.client.chat(this.sessionId, line);
                if (response.status() == 200) {
                    if (this.sessionId == null) this.sessionId = response.sessionId();
                    Log.answer(response.body());
                } else {
                    Log.error("Error " + response.status() + ": " + response.body());
                }
            }
        } catch (Exception e) {
            Log.error("Fatal: " + e.getMessage());
            System.exit(1);
        }

        Log.info("Bye.");
    }

    void handleCommand(String line) {
        var parts = line.split("\\s+", 2);
        var cmd = parts[0];

        switch (cmd) {
            case "/help" -> Log.info("""
                /help          Show this help
                /act [seed]    Trigger autonomous action
                /session       Show current session ID
                /quit, /exit   Exit""");
            case "/session" -> Log.info("Session: " + (this.sessionId != null ? this.sessionId : "(not yet established)"));
            case "/act" -> {
                var seed = parts.length > 1 ? parts[1] : "";
                var response = this.client.act(this.sessionId, seed);
                if (response.status() == 200) {
                    if (this.sessionId == null) this.sessionId = response.sessionId();
                    Log.answer(response.body());
                } else {
                    Log.error("Error " + response.status() + ": " + response.body());
                }
            }
            default -> Log.error("Unknown command: " + cmd + ". Type /help");
        }
    }

    void banner() {
        Log.info("zsmith-chat — connecting to " + this.config.host() + ":" + this.config.port());
        Log.info("Type a message to chat, /help for commands, /quit to exit");
        if (this.config.sessionId() != null) {
            Log.info("Resuming session: " + this.config.sessionId());
        }
        System.out.println();
    }

    public static void main(String[] args) {
        var config = Config.parse(args);
        new Chat(config).start();
    }
}

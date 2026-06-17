package airhacks.zsmith.tui.boundary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tui.control.ChatClient;
import airhacks.zsmith.tui.entity.Config;
import airhacks.zsmith.tui.entity.Response;

public class Chat {

    static final String HELP = """
            /help          Show this help
            /act [seed]    Trigger autonomous action
            /session       Show current session ID
            /quit, /exit   Exit""";

    ChatClient client;
    Config config;
    String sessionId;

    void start() {
        banner();
        try (var input = new BufferedReader(new InputStreamReader(System.in))) {
            prompt();
            while (input.readLine() instanceof String line && handle(line)) {
                prompt();
            }
        } catch (IOException problem) {
            Log.error("Fatal: " + problem.getMessage());
        }
        Log.info("Bye.");
    }

    boolean handle(String line) {
        var input = line.strip();
        if (input.isEmpty()) {
            return true;
        }
        if (input.equals("/quit") || input.equals("/exit")) {
            return false;
        }
        if (input.startsWith("/")) {
            command(input);
            return true;
        }
        show(this.client.chat(this.sessionId, input));
        return true;
    }

    void command(String line) {
        var parts = line.split("\\s+", 2);
        switch (parts[0]) {
            case "/help" -> Log.info(HELP);
            case "/session" -> Log.info("Session: " + (this.sessionId != null ? this.sessionId : "(not yet established)"));
            case "/act" -> show(this.client.act(this.sessionId, parts.length > 1 ? parts[1] : ""));
            default -> Log.error("Unknown command: " + parts[0] + ". Type /help");
        }
    }

    void show(Response response) {
        if (response.status() != 200) {
            Log.error("Error " + response.status() + ": " + response.body());
            return;
        }
        if (this.sessionId == null) {
            this.sessionId = response.sessionId();
        }
        Log.answer(response.body());
    }

    void banner() {
        Log.info("zsmith-chat — connecting to " + this.config.host() + ":" + this.config.port());
        Log.info("Type a message to chat, /help for commands, /quit to exit");
        if (this.config.sessionId() != null) {
            Log.info("Resuming session: " + this.config.sessionId());
        }
    }

    void prompt() {
        System.out.print("> ");
        System.out.flush();
    }

    void main(String... args) {
        try {
            this.config = Config.parse(args);
        } catch (IllegalArgumentException invalid) {
            Log.error(invalid.getMessage());
            return;
        }
        this.client = new ChatClient(this.config);
        this.sessionId = this.config.sessionId();
        start();
    }
}

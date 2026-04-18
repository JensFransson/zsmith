package airhacks.zsmith.tui.control;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tui.entity.Config;
import airhacks.zsmith.tui.entity.Response;

public class ChatClient {

    static final String SESSION_HEADER = "X-Session-Id";

    final HttpClient client;
    final Config config;

    public ChatClient(Config config) {
        this.config = config;
        this.client = HttpClient.newHttpClient();
    }

    public Response chat(String sessionId, String message) {
        return send(sessionId, "/chat", message);
    }

    public Response act(String sessionId, String seed) {
        return send(sessionId, "/act", seed);
    }

    Response send(String sessionId, String path, String body) {
        var uri = URI.create("http://" + this.config.host() + ":" + this.config.port() + path);
        var builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(this.config.timeout()))
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(BodyPublishers.ofString(body));
        if (sessionId != null) {
            builder.header(SESSION_HEADER, sessionId);
        }

        var waiting = new AtomicBoolean(true);
        var spinner = Thread.startVirtualThread(() -> {
            try {
                while (waiting.get()) {
                    System.out.print(".");
                    System.out.flush();
                    Thread.sleep(500);
                }
            } catch (InterruptedException _) {}
        });

        try {
            var response = this.client.send(builder.build(), BodyHandlers.ofString());
            waiting.set(false);
            spinner.join();
            System.out.println();
            var returnedSession = response.headers().firstValue(SESSION_HEADER).orElse(sessionId);
            return new Response(response.statusCode(), response.body(), returnedSession);
        } catch (java.net.ConnectException e) {
            waiting.set(false);
            try { spinner.join(); } catch (InterruptedException _) {}
            System.out.println();
            return new Response(-1, "Connection refused — is the server running on " + this.config.host() + ":" + this.config.port() + "?", sessionId);
        } catch (Exception e) {
            waiting.set(false);
            try { spinner.join(); } catch (InterruptedException _) {}
            System.out.println();
            return new Response(-1, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), sessionId);
        }
    }
}

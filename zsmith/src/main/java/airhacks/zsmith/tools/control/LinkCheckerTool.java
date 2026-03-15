package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.json.JSONObject;

public class LinkCheckerTool implements Tool {

    static final int CONNECT_TIMEOUT_SECONDS = 10;
    static final int REQUEST_TIMEOUT_SECONDS = 10;
    static final int MAX_BODY_LENGTH = 5000;

    private final HttpClient httpClient;

    public LinkCheckerTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .followRedirects(Redirect.NORMAL)
                .build();
    }

    @Override
    public String toolName() {
        return "check_link";
    }

    @Override
    public String description() {
        return "Checks a URL and returns response information including status code, content type, and body preview";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "url": {
                            "type": "string",
                            "description": "The URL to check"
                        }
                    },
                    "required": ["url"]
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        if (!input.has("url") || input.getString("url").isBlank()) {
            return "Error: Missing required parameter: url";
        }

        var urlString = input.getString("url");

        URI uri;
        try {
            uri = URI.create(urlString);
            if (uri.getScheme() == null) {
                return "Error: Invalid URL";
            }
        } catch (IllegalArgumentException e) {
            return "Error: Invalid URL";
        }

        try {
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            var statusCode = response.statusCode();
            var contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("unknown");
            var body = response.body();
            var truncatedBody = body.length() > MAX_BODY_LENGTH
                    ? body.substring(0, MAX_BODY_LENGTH)
                    : body;

            return "Status: %d\nContent-Type: %s\nBody (first 5000 chars):\n%s"
                    .formatted(statusCode, contentType, truncatedBody);

        } catch (HttpTimeoutException e) {
            return "Error: Connection timed out";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

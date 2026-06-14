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

public interface FetchUrlTool {

    int CONNECT_TIMEOUT_SECONDS = 10;
    int REQUEST_TIMEOUT_SECONDS = 15;
    int MAX_BODY_LENGTH = 20_000;
    String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .followRedirects(Redirect.NORMAL)
            .build();

    enum Field { url }

    static Tool create() {
        return Tool.of(
                "fetch_url",
                "Fetches the content of a URL using a browser User-Agent and returns status, content type, and up to 20000 chars of the body. Use this to retrieve page or API content; use check_link only for lightweight reachability verification.",
                Tool.schema(Tool.Prop.string(Field.url, "The URL to fetch")),
                FetchUrlTool::run,
                true);
    }

    private static String run(JSONObject input) {
        if (!input.has(Field.url.name()) || input.getString(Field.url.name()).isBlank()) {
            return "Error: Missing required parameter: url";
        }

        var urlString = input.getString(Field.url.name());

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
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            var statusCode = response.statusCode();
            var contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("unknown");
            var finalUri = response.uri();
            var body = response.body();
            var truncated = body.length() > MAX_BODY_LENGTH;
            var bodyOut = truncated ? body.substring(0, MAX_BODY_LENGTH) : body;

            return "Status: %d\nFinal-URL: %s\nContent-Type: %s\nBody-Length: %d%s\nBody:\n%s"
                    .formatted(statusCode, finalUri, contentType, body.length(),
                            truncated ? " (truncated to " + MAX_BODY_LENGTH + ")" : "",
                            bodyOut);

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

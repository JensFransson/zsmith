package airhacks.zsmith.tools.control;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.json.JSONObject;

public interface LinkCheckerTool {

    int CONNECT_TIMEOUT_SECONDS = 10;
    int REQUEST_TIMEOUT_SECONDS = 10;
    String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .followRedirects(Redirect.NORMAL)
            .build();

    enum Field { url }

    static ToolHandler create() {
        return ToolHandler.of(
                "check_link",
                "Verifies a URL is reachable. Returns status code, final URL after redirects, and content type. Use fetch_url to retrieve page or API content.",
                ToolHandler.schema(ToolHandler.Prop.string(Field.url, "The URL to check")),
                LinkCheckerTool::run,
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
            var response = send(uri, "HEAD");
            if (response.statusCode() == 405 || response.statusCode() == 501) {
                response = send(uri, "GET");
            }
            return format(response);
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

    private static HttpResponse<Void> send(URI uri, String method) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return HTTP_CLIENT.send(request, BodyHandlers.discarding());
    }

    private static String format(HttpResponse<?> response) {
        var contentType = response.headers().firstValue("Content-Type").orElse("unknown");
        return "Status: %d\nFinal-URL: %s\nContent-Type: %s"
                .formatted(response.statusCode(), response.uri(), contentType);
    }
}

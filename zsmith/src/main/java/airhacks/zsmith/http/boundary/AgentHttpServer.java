package airhacks.zsmith.http.boundary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import airhacks.zsmith.http.control.ActHandler;
import airhacks.zsmith.http.control.ChatHandler;
import airhacks.zsmith.http.control.Sessions;
import airhacks.zsmith.logging.control.Log;

public class AgentHttpServer {

    static final String HOST = "0.0.0.0";
    static final int BACKLOG = 0;

    final HttpServer server;

    AgentHttpServer(HttpServer server) {
        this.server = server;
    }

    public static AgentHttpServer start(ChatEngine engine, int port) {
        try {
            var server = HttpServer.create(new InetSocketAddress(HOST, port), BACKLOG);
            var sessions = new Sessions();
            server.createContext("/chat", new ChatHandler(engine, sessions));
            server.createContext("/act", new ActHandler(engine, sessions));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            Log.agent("HTTP server listening on " + HOST + ":" + server.getAddress().getPort());
            return new AgentHttpServer(server);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start HTTP server on port " + port, e);
        }
    }

    public int port() {
        return this.server.getAddress().getPort();
    }

    public void stop() {
        this.server.stop(0);
    }
}

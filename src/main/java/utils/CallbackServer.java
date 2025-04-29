package utils;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CallbackServer {
    public static void start(Consumer<String> onCodeReceived) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/fb-callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));

            String code = params.get("code");

            String response = "You can now return to the app.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();

            onCodeReceived.accept(code);
            server.stop(0);
        });
        new Thread(server::start).start();
    }
}

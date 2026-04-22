package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.mephi.malskiy.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"get".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        sendJson(exchange, 200, Map.of("status", "OK"));
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object response) throws IOException {
        byte[] responseByte = JsonUtil.toJson(response).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseByte.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseByte);
        }
    }
}

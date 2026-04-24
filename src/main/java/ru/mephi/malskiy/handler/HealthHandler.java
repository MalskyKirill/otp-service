package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"get".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        ResponseUtil.sendJson(exchange, 200, Map.of("status", "OK"));
    }
}


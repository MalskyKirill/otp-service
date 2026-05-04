package ru.mephi.malskiy.handler.middleware;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class LoggingHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoggingHandler.class);

    private final HttpHandler delegate;

    public LoggingHandler(HttpHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startNanos = System.nanoTime();
        StatusCapturingExchange capturingExchange = new StatusCapturingExchange(exchange);

        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri != null ? uri.getPath() : "unknown";

        try {
            delegate.handle(capturingExchange);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            int status = capturingExchange.getStatusCode();
            logger.info("HTTP request processed: method={}, path={}, status={}, latencyMs={}", method, path, status, elapsedMs);
        }
    }

    private static class StatusCapturingExchange extends HttpExchange {
        private final HttpExchange delegate;
        private int statusCode = 200;

        private StatusCapturingExchange(HttpExchange delegate) {
            this.delegate = delegate;
        }

        int getStatusCode() {
            return statusCode;
        }

        @Override
        public Headers getRequestHeaders() {
            return delegate.getRequestHeaders();
        }

        @Override
        public Headers getResponseHeaders() {
            return delegate.getResponseHeaders();
        }

        @Override
        public URI getRequestURI() {
            return delegate.getRequestURI();
        }

        @Override
        public String getRequestMethod() {
            return delegate.getRequestMethod();
        }

        @Override
        public HttpContext getHttpContext() {
            return delegate.getHttpContext();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public InputStream getRequestBody() {
            return delegate.getRequestBody();
        }

        @Override
        public OutputStream getResponseBody() {
            return delegate.getResponseBody();
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            this.statusCode = rCode;
            delegate.sendResponseHeaders(rCode, responseLength);
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return delegate.getRemoteAddress();
        }

        @Override
        public int getResponseCode() {
            return statusCode;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return delegate.getLocalAddress();
        }

        @Override
        public String getProtocol() {
            return delegate.getProtocol();
        }

        @Override
        public Object getAttribute(String name) {
            return delegate.getAttribute(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            delegate.setAttribute(name, value);
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
            delegate.setStreams(i, o);
        }

        @Override
        public com.sun.net.httpserver.HttpPrincipal getPrincipal() {
            return delegate.getPrincipal();
        }
    }
}

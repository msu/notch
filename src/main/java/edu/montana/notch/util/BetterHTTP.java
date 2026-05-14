package edu.montana.notch.util;

import edu.montana.notch.json5.JSON5Array;
import edu.montana.notch.json5.JSON5Object;
import edu.montana.notch.json5.JSON5Value;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.logging.Logger;

public class BetterHTTP {
    private static final Logger LOGGER = Logger.getLogger(BetterHTTP.class.getName());

    private final Request request;
    private Logger logger = LOGGER;

    private BetterHTTP(Request req) {
        this.request = req;
    }

    public static BetterHTTP GET(String uri) {
        var req = new Request();
        req.method = "GET";
        req.uri = URI.create(uri);
        return new BetterHTTP(req);
    }

    public static BetterHTTP GET(URI uri) {
        var req = new Request();
        req.method = "GET";
        req.uri = uri;
        return new BetterHTTP(req);
    }

    public static BetterHTTP POST(URI uri) {
        var req = new Request();
        req.method = "POST";
        req.uri = uri;
        return new BetterHTTP(req);
    }

    public static BetterHTTP POST(String uri) {
        var req = new Request();
        req.method = "POST";
        req.uri = URI.create(uri);
        return new BetterHTTP(req);
    }

    public static BetterHTTP PUT(String uri) {
        var req = new Request();
        req.method = "PUT";
        req.uri = URI.create(uri);
        return new BetterHTTP(req);
    }

    public static BetterHTTP PUT(URI uri) {
        var req = new Request();
        req.method = "PUT";
        req.uri = uri;
        return new BetterHTTP(req);
    }

    public static BetterHTTP DELETE(String uri) {
        var req = new Request();
        req.method = "DELETE";
        req.uri = URI.create(uri);
        return new BetterHTTP(req);
    }

    public static BetterHTTP DELETE(URI uri) {
        var req = new Request();
        req.method = "DELETE";
        req.uri = uri;
        return new BetterHTTP(req);
    }

    public Response<String> fetch() {
        return fetch(HttpResponse.BodyHandlers.ofString());
    }

    public <T> Response<T> fetch(HttpResponse.BodyHandler<T> handler) {
        try {
            var client = HttpClient.newHttpClient();
            var httpReq = HttpRequest.newBuilder();
            httpReq.uri(request.uri);
            httpReq.method(request.method, request.body);
            if (request.timeout != null) httpReq.timeout(request.timeout);
            for (Header header : request.headers) {
                httpReq.header(header.key, header.value);
            }

            if (logger != null) {
                logger.info("sending HTTP %s %s".formatted(request.method, request.uri));
            }

            var res = client.send(httpReq.build(), handler);

            if (logger != null) {
                logger.info("got HTTP %s %s : %d".formatted(request.method, request.uri, res.statusCode()));
            }
            return new Response<>(res);
        } catch (Exception e) {
            throw Exceptions.rethrow(e);
        }
    }

    public BetterHTTP withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public BetterHTTP withHeader(String name, String value) {
        request.headers.add(new Header(name, value));
        return this;
    }

    public BetterHTTP withJsonData(JSON5Value json) {
        withHeader("content-type", "application/json");
        withBody(HttpRequest.BodyPublishers.ofString(json.encode()));
        return this;
    }

    public BetterHTTP withJsonData(Object... values) {
        var json = new JSON5Object(values);
        return withJsonData(json);
    }

    public BetterHTTP withFormData(Object... values) {
        var formData = formUrlEncode(values);
        withHeader("content-type", "application/x-www-form-urlencoded");
        withBody(HttpRequest.BodyPublishers.ofString(formData));
        return this;
    }

    public BetterHTTP withBody(HttpRequest.BodyPublisher body) {
        request.body = body;
        return this;
    }

    public BetterHTTP withTimeout(Duration timeout) {
        request.timeout = timeout;
        return this;
    }

    public static String formUrlEncode(Object... values) {
        assert values.length % 2 == 0;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            String key = ((String) values[i]);
            Object value = values[i + 1];
            key = URLEncoder.encode(key, StandardCharsets.UTF_8);
            value = URLEncoder.encode("" + value, StandardCharsets.UTF_8);
            if (i > 0) out.append("&");
            out.append(key).append("=").append(value);
        }
        return out.toString();
    }


    public record Header(String key, String value) {
    }

    public static final class Request {
        String method;
        URI uri;
        final LinkedList<Header> headers = new LinkedList<>();
        Duration timeout = null;
        HttpRequest.BodyPublisher body;

        Request() {
        }
    }

    public record Response<T>(HttpResponse<T> inner) {
        public Response<T> assertOk(String message) {
            if (inner.statusCode() != 200) {
                String errM = message + ", " + inner.request().method() + " " + inner.request().uri() + ", got status code " + inner.statusCode();
                if (inner.body() instanceof String s && !s.isBlank()) {
                    errM += ": " + s;
                }
                var error = new AssertionError(errM);
                error.setStackTrace(Thread.currentThread().getStackTrace());
                throw error;
            }
            return this;
        }

        public JSON5Object json() {
            var body = inner.body();
            var req = this.inner.request();
            var id = req.method() + " " + req.uri();
            return JSON5.parseObject(id, (String) body);
        }

        public JSON5Array jsonArray() {
            var body = inner.body();
            var req = this.inner.request();
            var id = req.method() + " " + req.uri();
            return JSON5.parseArray(id, (String) body);
        }

        public String string() {
            return ((String) inner.body());
        }
    }
}

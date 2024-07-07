package http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import http.request.HttpRequest;
import http.response.HttpResponse;

public class Dispatcher {
    public void dispatch(HttpRequest request, OutputStream out) throws IOException {
        String target = request.getTarget();
        if (target.equals("/") || target.equals("/index.html")) {
            try (InputStream is = getClass().getResourceAsStream("/static/index.html")) {
                HttpResponse.ok(is).write(out);
            }
            return;
        }

        if (!target.contains("..") /** avoid directory traversal */ && (target.startsWith(
                "/css/") || target.startsWith("/js/") || target.startsWith("/images/"))) {
            try (InputStream is = getClass().getResourceAsStream("/static/" + target)) {
                HttpResponse.ok(is).write(out);
            }
            return;
        }

        HttpResponse.notFound("The requested URL %s was not found on this server.".formatted(target))
                .write(out);
    }
}

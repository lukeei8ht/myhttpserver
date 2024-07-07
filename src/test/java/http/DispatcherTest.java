package http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import http.request.HttpMethod;
import http.request.HttpRequest;

class DispatcherTest {
    @Test
    void get_index() throws IOException, URISyntaxException {
        HttpRequest httpRequest = HttpRequest.builder(HttpMethod.GET).target("/index.html").build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new Dispatcher().dispatch(httpRequest, out);

        assertEquals("HTTP/1.1 200 OK\r\nContent-Length: 404\r\n\r\n%s".formatted(
                        Files.readString(Paths.get(getClass().getResource("/static/index.html").toURI()))),
                out.toString());
    }

    @Test
    void get_index_from_root() throws IOException, URISyntaxException {
        HttpRequest httpRequest = HttpRequest.builder(HttpMethod.GET).target("/").build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new Dispatcher().dispatch(httpRequest, out);

        assertEquals("HTTP/1.1 200 OK\r\nContent-Length: 404\r\n\r\n%s".formatted(
                        Files.readString(Paths.get(getClass().getResource("/static/index.html").toURI()))),
                out.toString());
    }

    @Test
    void unsupported_target_response_not_found_error() throws IOException {
        HttpRequest httpRequest = HttpRequest.builder(HttpMethod.GET).target("/not-found").build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new Dispatcher().dispatch(httpRequest, out);

        assertEquals(
                "HTTP/1.1 404 Not Found\r\nConnection: close\r\nContent-Length: 58\r\n\r\nThe requested URL /not-found was not found on this server.",
                out.toString());
    }

    @Test
    void protect_directory_traversal() throws IOException {
        HttpRequest httpRequest = HttpRequest.builder(HttpMethod.GET).target("/css/../images/http.png")
                .build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new Dispatcher().dispatch(httpRequest, out);

        assertEquals(
                "HTTP/1.1 404 Not Found\r\nConnection: close\r\nContent-Length: 71\r\n\r\nThe requested URL /css/../images/http.png was not found on this server.",
                out.toString());
    }
}
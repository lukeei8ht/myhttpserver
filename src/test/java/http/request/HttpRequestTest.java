package http.request;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class HttpRequestTest {
    @Test
    void parse_request_success() throws IOException {
        String request = """
                GET /index.html HTTP/1.1
                Host: localhost:8080
                                
                """;
        HttpRequest httpRequest = HttpRequest.fromInputStream(
                new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

        assertEquals(HttpMethod.GET, httpRequest.getMethod());
        assertEquals("/index.html", httpRequest.getTarget());
        assertEquals(Map.of("host", "localhost:8080"), httpRequest.getHeaders());
    }

    @Test
    void parse_request_non_ascii_charset_header() throws IOException {
        String request = """
                GET /index.html HTTP/1.1
                Host: localhost:8080
                あああ: いいい
                                
                """;
        HttpRequest httpRequest = HttpRequest.fromInputStream(
                new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

        assertEquals(HttpMethod.GET, httpRequest.getMethod());
        assertEquals("/index.html", httpRequest.getTarget());
        assertEquals(Map.of("host", "localhost:8080",
                        new String("あああ".getBytes(StandardCharsets.UTF_8), StandardCharsets.US_ASCII),
                        new String("いいい".getBytes(StandardCharsets.UTF_8), StandardCharsets.US_ASCII)),
                httpRequest.getHeaders());
    }

    @Test
    void addHeader_non_space() {
        Map<String, String> headers = new HashMap<>();
        HttpRequest.addHeader(headers, "Host:localhost:8080");
        assertEquals(Map.of("host", "localhost:8080"), headers);
    }

    @Test
    void addHeader_tab_space() {
        Map<String, String> headers = new HashMap<>();
        HttpRequest.addHeader(headers, "Host:\tlocalhost:8080");
        assertEquals(Map.of("host", "localhost:8080"), headers);
    }

    @Test
    void addHeader_invalid_space_as_value() {
        Map<String, String> headers = new HashMap<>();
        HttpRequest.addHeader(headers, "Host:　localhost:8080");
        assertEquals(Map.of("host", "　localhost:8080"), headers);
    }
}
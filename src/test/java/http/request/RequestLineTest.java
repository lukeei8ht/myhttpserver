package http.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestLineTest {
    @Test
    void success() {
        String line = "GET /index.html HTTP/1.1";
        RequestLine requestLine = RequestLine.fromString(line);

        assertEquals(HttpMethod.GET, requestLine.method());
        assertEquals("/index.html", requestLine.target());
        assertEquals("HTTP/1.1", requestLine.version());
    }

    @Test
    void invalid_request_line() {
        String line = "a";
        IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class,
                () -> RequestLine.fromString(line));
        assertEquals("Invalid request line: a", exception.getMessage());
    }

    @Test
    void invalid_method() {
        String line = "get /index.html HTTP/1.1";
        IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class,
                () -> RequestLine.fromString(line));
        assertEquals("Unsupported HTTP method: get", exception.getMessage());
    }

    @Test
    void invalid_target_space() {
        String line = "GET / index.html HTTP/1.1";
        IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class,
                () -> RequestLine.fromString(line));
        assertEquals("Invalid request line: GET / index.html HTTP/1.1", exception.getMessage());
    }

    @Test
    void invalid_version() {
        String line = "GET /index.html HTTP";
        IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class,
                () -> RequestLine.fromString(line));
        assertEquals("Unsupported HTTP version: HTTP", exception.getMessage());
    }
}
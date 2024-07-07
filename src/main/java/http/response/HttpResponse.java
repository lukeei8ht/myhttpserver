package http.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import http.request.HttpRequest;

public class HttpResponse {
    private final String version = "HTTP/1.1";

    private final Status status;

    private final Map<String, String> headers = new HashMap<>();

    private final InputStream body;

    private final int contentLength;

    private HttpResponse(Status status, String message) {
        this.status = status;
        this.body = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        this.contentLength = message.length();
    }

    private HttpResponse(Status status, InputStream body) {
        this.status = status;
        this.body = body;
        this.contentLength = -1;
    }

    public static HttpResponse fromThrowable(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return new HttpResponse(Status.BAD_REQUEST, t.getMessage()).withCloseConnection();
        }

        return new HttpResponse(Status.INTERNAL_SERVER_ERROR, t.getMessage()).withCloseConnection();
    }

    public static HttpResponse ok(InputStream body) {
        return new HttpResponse(Status.OK, body);
    }

    public static HttpResponse notFound(String message) {
        return new HttpResponse(Status.NOT_FOUND, message);
    }

    public HttpResponse addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public HttpResponse withCloseConnection() {
        headers.put("Connection", "close");
        return this;
    }

    public boolean isCloseConnection() {
        return "close".equals(headers.get("Connection"));
    }

    public HttpResponse write(OutputStream out) throws IOException {
        out.write("%s %d %s".formatted(version, status.getCode(), status.getMessage())
                .getBytes(StandardCharsets.US_ASCII));

        if (contentLength > 0) {
            headers.put("Content-Length", String.valueOf(contentLength));
        } else if (body != null) {
            headers.put("Transfer-Encoding", "chunked");
        }

        for (var header : headers.entrySet()) {
            out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            out.write("%s: %s".formatted(header.getKey(), header.getValue())
                    .getBytes(StandardCharsets.US_ASCII));
        }

        out.write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));

        if (body != null) {
            boolean chunked = contentLength <= 0;
            byte[] buffer = new byte[1 << 13];
            int len;
            while ((len = body.read(buffer)) != -1) {
                if (chunked) {
                    out.write("%x\r\n".formatted(len).getBytes(StandardCharsets.US_ASCII));
                }
                out.write(buffer, 0, len);
                if (chunked) {
                    out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                }
            }
            if (chunked) {
                out.write("%x\r\n\r\n".formatted(0).getBytes(StandardCharsets.US_ASCII));
            }
        }

        return this;
    }
}

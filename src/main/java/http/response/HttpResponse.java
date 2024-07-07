package http.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private final String version = "HTTP/1.1";

    private final Status status;

    private final InputStream body;

    private HttpResponse(Status status, String message) {
        this.status = status;
        this.body = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
    }

    private HttpResponse(Status status, InputStream body) {
        this.status = status;
        this.body = body;
    }

    public static HttpResponse fromThrowable(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return new HttpResponse(Status.BAD_REQUEST, t.getMessage());
        }

        return new HttpResponse(Status.INTERNAL_SERVER_ERROR, t.getMessage());
    }

    public static HttpResponse ok(InputStream body) {
        return new HttpResponse(Status.OK, body);
    }

    public static HttpResponse notFound(String message) {
        return new HttpResponse(Status.NOT_FOUND, message);
    }

    public void write(OutputStream out) throws IOException {
        out.write("%s %d %s".formatted(version, status.getCode(), status.getMessage())
                .getBytes(StandardCharsets.US_ASCII));

        out.write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));

        if (body != null) {
            byte[] buffer = new byte[1 << 13];
            int len;
            while ((len = body.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}

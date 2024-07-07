package http.response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private final String version = "HTTP/1.1";

    private final Status status;

    private final String message;

    public HttpResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static HttpResponse fromThrowable(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return new HttpResponse(Status.BAD_REQUEST, t.getMessage());
        }

        return new HttpResponse(Status.INTERNAL_SERVER_ERROR, t.getMessage());

    }

    public void write(OutputStream out) throws IOException {
        out.write("%s %d %s".formatted(version, status.getCode(), status.getMessage())
                .getBytes(StandardCharsets.UTF_8));

        out.write("\r\n\r\n".getBytes(StandardCharsets.UTF_8));

        if (message != null) {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        }
    }
}

package http.response;

public enum Status {
    OK(200, "OK"), BAD_REQUEST(400, "Bad Request"), INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;

    private final String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

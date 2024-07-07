package http.request;

import java.util.List;

record RequestLine(HttpMethod method, String target, String version) {

    private static final List<String> SUPPORT_VERSIONS = List.of("HTTP/1.1");

    public static RequestLine fromString(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid request line: " + line);
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + parts[0]);
        }

        String target = parts[1];
        String version = parts[2];
        if (!SUPPORT_VERSIONS.contains(version)) {
            throw new IllegalArgumentException("Unsupported HTTP version: " + version);
        }

        return new RequestLine(method, target, version);
    }
}
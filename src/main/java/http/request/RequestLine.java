package http.request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record RequestLine(HttpMethod method, String target, String version) {

    // https://datatracker.ietf.org/doc/html/rfc9112#section-3
    private static final Pattern requestLinePattern
            = Pattern.compile("^(?<method>\\S+) (?<target>\\S+) (?<version>\\S+)$");

    private static final List<String> SUPPORT_VERSIONS = List.of("HTTP/1.1");

    public static RequestLine fromString(String line) throws IllegalArgumentException {
        Matcher matcher = requestLinePattern.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid request line: " + line);
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(matcher.group("method"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + matcher.group("method"));
        }

        String target = matcher.group("target"); // TODO パーセントデコード
        String version = matcher.group("version");
        if (!SUPPORT_VERSIONS.contains(version)) {
            throw new IllegalArgumentException("Unsupported HTTP version: " + version);
        }

        return new RequestLine(method, target, version);
    }
}
package http.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private final RequestLine requestLine;

    private final Map<String, String> headers;

    private HttpRequest(RequestLine requestLine, Map<String, String> headers) {
        this.requestLine = requestLine;
        this.headers = Map.copyOf(headers);
    }

    // https://datatracker.ietf.org/doc/html/rfc9112#name-field-syntax
    private static Pattern headerPattern = Pattern.compile("^(?<key>[^:]+):[ \\t]?(?<value>.+)[ \\t]?$");

    /**
     * ヘッダーマップにヘッダーを追加する<br>
     * キーは小文字で格納される
     *
     * @param headers    ヘッダーマップ
     * @param headerLine ヘッダー行文字列
     */
    static void addHeader(Map<String, String> headers, String headerLine) {
        Matcher matcher = headerPattern.matcher(headerLine);
        if (matcher.matches()) {
            headers.put(matcher.group("key").toLowerCase(), matcher.group("value"));
        } else {
            throw new IllegalArgumentException("Invalid header field: " + headerLine);
        }
    }

    /**
     * 入力ストリームをパースしてHttpRequestを生成する
     *
     * @param in 入力ストリーム
     * @return HttpRequest
     * @throws IOException              予期せぬI/Oエラーが発生した場合
     * @throws IllegalArgumentException リクエストが不正な場合
     */
    public static HttpRequest fromInputStream(InputStream in) throws IOException, IllegalArgumentException {
        RequestLine requestLine = null;
        Map<String, String> headers = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) { // end of field-line
                break;
            }

            if (firstLine) {
                requestLine = RequestLine.fromString(line);
                firstLine = false;
            } else {
                addHeader(headers, line);
            }
        }

        if (requestLine == null) {
            throw new IllegalArgumentException("Invalid http request");
        }

        // TODO parse message-body

        return new HttpRequest(requestLine, headers);
    }

    @Override
    public String toString() {
        return "HttpRequest{method=%s, target=%s, headers=%s}".formatted(getMethod(), getTarget(),
                getHeaders());
    }

    public HttpMethod getMethod() {
        return requestLine.method();
    }

    public String getTarget() {
        return requestLine.target();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }
}

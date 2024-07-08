package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import http.request.HttpRequest;
import http.response.HttpResponse;

public class HttpServer {

    private final int port;

    private final int keepAliveTimeout = (int) TimeUnit.SECONDS.toMillis(60);

    private volatile boolean running;

    public HttpServer(int port) {
        this.port = port;
        this.running = true;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);

            ExecutorService pool = Executors.newFixedThreadPool(100);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    pool.execute(() -> task(socket));
                } catch (SocketTimeoutException ignore) {
                }
            }
        }
    }

    private void task(Socket socket) {
        try (socket; InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            socket.setSoTimeout(keepAliveTimeout);
            String connection = "keep-alive";
            while (!"close".equals(connection)) {
                HttpRequest httpRequest;
                try {
                    httpRequest = HttpRequest.fromInputStream(in);
                    connection = httpRequest.getHeader("Connection");
                } catch (SocketTimeoutException closeConnection) {
                    return;
                } catch (Throwable t) {
                    HttpResponse.fromThrowable(t).write(out);
                    return;
                }

                HttpResponse httpResponse = new Dispatcher().dispatch(httpRequest, out);
                if (httpResponse.isCloseConnection()) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void shutdown() {
        this.running = false;
    }
}

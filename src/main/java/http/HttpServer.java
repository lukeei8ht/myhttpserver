package http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;

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
        try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            byte[] buffer = new byte[1 << 13];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void shutdown() {
        this.running = false;
    }
}

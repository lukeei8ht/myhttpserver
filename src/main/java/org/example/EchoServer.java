package org.example;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class EchoServer {

    private final int port;

    private volatile boolean running;

    EchoServer(int port) {
        this.port = port;
        this.running = true;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);

            while (running) {
                try (Socket socket = serverSocket.accept(); InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                    byte[] buffer = new byte[1 << 13];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                } catch (SocketTimeoutException ignore) {
                }
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }
}

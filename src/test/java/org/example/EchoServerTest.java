package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EchoServerTest {

    private static EchoServer server;

    private static final int port = 11111;

    @BeforeAll
    static void beforeAll() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                server = new EchoServer(port);
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    static void afterAll() {
        server.shutdown();
    }

    @Test
    void test() throws IOException {
        try (Socket socket = new Socket("localhost",
                port); OutputStream out = socket.getOutputStream(); InputStream in = socket.getInputStream()) {
            PrintWriter writer = new PrintWriter(out, true);
            writer.println("test");
            writer.println("test");

            ByteArrayOutputStream dump = new ByteArrayOutputStream();
            byte[] buffer = new byte[1 << 13];
            int len;
            while (in.available() > 0 && (len = in.read(buffer)) != -1) {
                dump.write(buffer, 0, len);
            }
            assertEquals("""
                    test
                    test
                    """, dump.toString());
        }
    }
}
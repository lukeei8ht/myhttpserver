package org.example;

import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        try {
            new EchoServer(8080).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
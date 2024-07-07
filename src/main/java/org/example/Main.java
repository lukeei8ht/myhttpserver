package org.example;

import http.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            new HttpServer(8080).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
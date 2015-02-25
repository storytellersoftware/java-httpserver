package tests.mocks;

import httpserver.HttpServer;

public class MockHttpServer {
    public static HttpServer mockServer() {
        return new HttpServer() {
            @Override public void run() {
                // do nothing
            }
        };
    }
}

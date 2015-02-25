package tests;

import org.junit.Test;
import static org.junit.Assert.fail;

import httpserver.HttpServer;
import httpserver.HttpRequest;
import httpserver.HttpResponse;
import httpserver.Route;

public class ServerAsAppTest {
    class MockHttpServer extends HttpServer {
        @Override public void run() {
            // do nothing.
        }
    }

    @Test
    public void dummyMainTest() {
        // The goal of this test is to just test what a really simple main
        // method for a basic server would look like, and confirm that it would
        // work

        try {
            HttpServer server = new MockHttpServer();

            server.get("/", new Route() {
                @Override public void handle(HttpRequest request, HttpResponse response) {
                    response.setBody("Hello World!");
                }
            });

            server.run();
        } catch (Throwable t) { // any throwable, including RuntimeExcpetions
            t.printStackTrace();
            fail("Couldn't create simple server with basic route");
        }
    }
}

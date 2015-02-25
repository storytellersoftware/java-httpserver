package tests;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import httpserver.HttpException;
import httpserver.HttpHandler;
import httpserver.HttpRequest;
import httpserver.HttpResponse;
import httpserver.HttpRouter;
import httpserver.Route;

import java.util.Map;
import java.net.ServerSocket;

import org.junit.Test;

public class HandlerTest extends HttpHandler {
    public HandlerTest() throws HttpException {
        get("/showHeaders", new Route() {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Headers:" + headerString(request.getHeaders()));
            }
        });

        get("/hello", new Route() {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello World!");
            }
        });


        get("/hello/{firstName}", new Route() {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + "!");
            }
        });

        get("/hello/{firstName}/{lastName}", new Route() {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + " " + request.getParam("lastName") + "!");
            }
        });

        get("/hello/{*}", new Route() {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                StringBuilder b = new StringBuilder();
                for (String name: request.getVarargs()) {
                    b.append("Hello ");
                    b.append(name);
                    b.append("!\n");
                }

                response.setBody(b.toString());
            }
        });
    }

    public static String headerString(Map<String, String> headers) {
        StringBuilder b = new StringBuilder();
        for (String key: headers.keySet()) {
            b.append("\n\t");
            b.append(key);
            b.append(":\t");
            b.append(headers.get(key));
            b.append("\n");
        }

        return b.toString();
    }

    public static ServerSocket makeServer() throws Exception {
        HttpRouter router = new HttpRouter();
        router.setDefaultHandler(new HandlerTest());
        HttpRequest.setRouter(router);

        return new ServerSocket(MockClient.DESIRED_PORT);
    }

    @Test
    public void testHandlerCreation() {
        try {
            HttpRouter f = new HttpRouter();
            f.addHandler("/", new HandlerTest());
        } catch (HttpException e) {
            e.printStackTrace();
            fail("Couldn't create new handler...");
        }
    }

    @Test
    public void testShowHeaders() {
        try {
            ServerSocket server = makeServer();

            MockClient client = new MockClient();
            client.setPath("/showHeaders");
            client.fillInSocket();

            HttpRequest request = new HttpRequest(server.accept());
            request.parseRequest();
            HttpResponse response = new HttpResponse(request);
            (new HandlerTest()).handle(request, response);

            server.close();

            String responseBody = new String(response.getBody(), "UTF-8");
            String expectedBody = "Headers:" + headerString(request.getHeaders());

            assertEquals(expectedBody, responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred in testShowHeaders");
        }
    }
}

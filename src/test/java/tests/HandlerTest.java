package tests;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;
import httpserver.HTTPResponse;
import httpserver.HTTPRouter;
import httpserver.Route;

import java.util.Map;
import java.net.ServerSocket;

import org.junit.Test;

public class HandlerTest extends HTTPHandler {
    public HandlerTest() throws HTTPException {
        get("/showHeaders", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
                response.setBody("Headers:" + headerString(request.getHeaders()));
            }
        });

        get("/hello", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
                response.setBody("Hello World!");
            }
        });


        get("/hello/{firstName}", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + "!");
            }
        });

        get("/hello/{firstName}/{lastName}", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + " " + request.getParam("lastName") + "!");
            }
        });

        get("/hello/{*}", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
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
        HTTPRouter router = new HTTPRouter();
        router.setDefaultHandler(new HandlerTest());
        HTTPRequest.setRouter(router);

        return new ServerSocket(MockClient.DESIRED_PORT);
    }

    @Test
    public void testHandlerCreation() {
        try {
            HTTPRouter f = new HTTPRouter();
            f.addHandler("/", new HandlerTest());
        } catch (HTTPException e) {
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

            HTTPRequest request = new HTTPRequest(server.accept());
            request.parseRequest();
            HTTPResponse response = new HTTPResponse(request);
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

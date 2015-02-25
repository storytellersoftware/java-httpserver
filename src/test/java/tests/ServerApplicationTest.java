package tests;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import httpserver.HttpException;
import httpserver.HttpHandler;
import httpserver.HttpRequest;
import httpserver.HttpResponse;
import httpserver.HttpRouter;
import httpserver.HttpServer;
import httpserver.Route;

import java.util.Map;
import java.net.ServerSocket;

import org.junit.Test;
import org.junit.BeforeClass;

import tests.mocks.MockHttpServer;
import tests.mocks.MockClient;

public class ServerApplicationTest {
    private static HttpServer server;

    @BeforeClass
    public static void setupServer() {
        server = MockHttpServer.mockServer();

        server.get(new Route("/showHeaders") {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Headers:" + headerString(request.getHeaders()));
            }
        });

        server.get(new Route("/hello") {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello World!");
            }
        });


        server.get(new Route("/hello/{firstName}") {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + "!");
            }
        });

        server.get(new Route("/hello/{firstName}/{lastName}") {
            @Override public void handle(HttpRequest request, HttpResponse response) {
                response.setBody("Hello " + request.getParam("firstName") + " " + request.getParam("lastName") + "!");
            }
        });

        server.get(new Route("/hello/{*}") {
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

    public static HttpResponse getResponse(MockClient client) throws Exception {
        ServerSocket socket = new ServerSocket(MockClient.DESIRED_PORT);
        client.fillInSocket();

        HttpRequest request = new HttpRequest(server.getRouter(), socket.accept());
        HttpResponse response = request.createResponse();
        socket.close();

        return response;
    }

    @Test
    public void testShowHeaders() {
        try {
            MockClient client = new MockClient();
            client.setPath("showHeaders");

            ServerSocket socket = new ServerSocket(MockClient.DESIRED_PORT);
            client.fillInSocket();

            HttpRequest request = new HttpRequest(server.getRouter(), socket.accept());
            HttpResponse response = request.createResponse();
            socket.close();

            String responseBody = new String(response.getBody(), "UTF-8");
            String expectedBody = "Headers:" + headerString(request.getHeaders());

            assertEquals(expectedBody, responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred in testShowHeaders");
        }
    }

    @Test
    public void testHello() {
        try {
            MockClient client = new MockClient();
            client.setPath("/hello");

            HttpResponse response = getResponse(client);

            assertEquals("Hello World!", new String(response.getBody(), "UTF-8"));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception occurred in testHello()");
        }
    }

    @Test
    public void testHelloName() {
        try {
            MockClient client = new MockClient();
            client.setPath("/hello/Don");
            HttpResponse response = getResponse(client);

            assertEquals("Hello Don!", new String(response.getBody(), "UTF-8"));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception occurred in testHelloName()");
        }
    }
}

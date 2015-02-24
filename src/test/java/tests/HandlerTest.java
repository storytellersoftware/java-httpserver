package tests;

import static org.junit.Assert.fail;
import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;
import httpserver.HTTPResponse;
import httpserver.HTTPRouter;
import httpserver.Route;

import org.junit.Test;

public class HandlerTest extends HTTPHandler {
    public HandlerTest() throws HTTPException {
        get("/showHeaders", new Route() {
            @Override public void handle(HTTPRequest request, HTTPResponse response) {
                StringBuilder b = new StringBuilder();
                for (String key: request.getHeaders().keySet()) {
                    b.append("\n\t");
                    b.append(key);
                    b.append(":\t");
                    b.append(request.getHeaders().get(key));
                    b.append("\n");
                }

                response.setBody("Headers:" + b.toString());
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

}

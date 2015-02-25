package httpserver;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * An HttpHandler is what all handlers used by your server descend from. <p>
 *
 * Extended classes have two options for determining their actions: they may
 * override the handle method (slightly harder), or use the addGet and addPost
 * methods in the constructor. See their descriptions for more information. <p>
 *
 * If you just want to send a static message to the client, regardless of
 * request, you can use a MessageHandler, instead of creating a new Handler.
 *
 * @see HttpHandler#handle
 * @see HttpHandler#addGET
 * @see HttpHandler#addPOST
 * @see MessageHandler
 */
public abstract class HttpHandler {
    private final HashMap<String, ArrayList<Route>> routes = new HashMap<>();

    private Socket socket;
    private DataOutputStream writer;


    /**
     * Create an HttpHandler. <p>
     *
     * When writing your own HttpHandler, this is where you should add the
     * handler's internal routing, as well performing any setup tasks. Handlers
     * are multi-use, which means that only one of any kind of handler should be
     * created in an application (unless you have custom needs).
     *
     * @throws HttpException  The exception typically comes from trying to add
     *                        a new method. In a standard configuration this will
     *                        keep the server from starting.
     */
    public HttpHandler() { }


    /**
     * Where the Handler handles the information given from the request and
     * based off of the paths specified in the Handler. <p>
     *
     * This can be overridden for more fine-grained handling. As is, it uses
     * the data behind the addGET, addPOST, and addDELETE methods for determining
     * the correct action to take. <p>
     *
     * If there is not exact match, the `*` path is used. If you don't have a `*`
     * catchall route, a 501 (Not implemented) is sent to the client.
     *
     * @param request     The incoming HttpRequest.
     * @param response    The outgoing HttpResponse, waiting to be filled by an
     *                    HttpHandler.
     *
     * @see HttpHandler#addGET
     * @see HttpHandler#addPOST
     * @see HttpHandler#addDELETE
     * @see HttpResponse#NOT_A_METHOD_ERROR
     */
    public void handle(HttpRequest request, HttpResponse response) {
        String httpRequestType = request.getRequestType().toUpperCase();
        if (!routes.containsKey(httpRequestType)) {
            response.message(501, "No " + httpRequestType + " routes exist.");
            return;
        }

        Route route = null;
        int bestFit = 0;
        for (Route testRoute : routes.get(httpRequestType)) {
            if (testRoute.matchesPerfectly(request.getSplitPath())) {
                route = testRoute;
                break;
            }

            int testScore = testRoute.howCorrect(request.getSplitPath());
            if (testScore > bestFit) {
                route = testRoute;
                bestFit = testScore;
            }
        }

        if (route == null) {
            response.message(501, HttpResponse.NOT_A_METHOD_ERROR);
            return;
        }

        route.invoke(request, response);
    }

    /**
     * Attach a method to a GET request at a path. <p>
     *
     * Methods are passed in as a String, and must be a member of the current
     * handler.<p>
     *
     * Path's should come in "/path/to/action" form. If the method requires
     * any parameters that aren't an HttpResponse, HttpRequest, or Map,
     * they should be included in the path, in the order they're
     * listed in the method header, in "{ClassName}" form. Example:
     * <code>/hello/{String}/{String}</code> is a good path. <p>
     *
     * Methods being passed in must accept an HttpResponse as their first
     * parameter. Methods may optionally accept an HttpRequest and a
     * Map&lt;String, String&gt; in that order (they may accept a Map but not an
     * HttpRequest, but if they accept both the HttpRequest must come first).
     *
     * Parameters following the above must be included in the java.lang library
     * and have a constructor that takes in a String.
     * Any other type of parameter will cause an exception to occur. <p>
     *
     * Additionally, primitives are not permited, because they're not classes in
     * the java.lang library. The three most common parameter types are String,
     * Integer, and Double.
     *
     * @param path        Path to match
     * @param methodName  Method belonging to the current class, in String form.
     * @throws HttpException When you do bad things.
     *
     * @see HttpHandler#addPOST
     * @see HttpHandler#addDELETE
     * @see HttpResponse
     * @see HttpRequest
     */
    public void get(Route route) {
        addRoute(HttpRequest.GET_REQUEST_TYPE, route);
    }

    /**
     * Attach a method to a POST request at a path. <p>
     *
     * For a more detailed explanation, see {@link HttpHandler#addGET}.
     *
     * @param path         Path to match
     * @param methodName   Class and Method in class#method form.
     * @throws HttpException When you do bad things.
     *
     * @see HttpHandler#addGET
     * @see HttpHandler#addDELETE
     */
    public void post(Route route) {
        addRoute(HttpRequest.POST_REQUEST_TYPE, route);
    }

    /**
     * Attach a method to a DELETE request at a path. <p>
     *
     * For a more detailed explanation, see {@link HttpHandler#addGET}.
     *
     * @param path        Path to match
     * @param methodName  Class and Method in class#method form.
     * @throws HttpException when you do bad things.
     *
     * @see HttpHandler#addGET
     * @see HttpHandler#addPOST
     */
    public void delete(Route route) {
        addRoute(HttpRequest.DELETE_REQUEST_TYPE, route);
    }

    /**
     * Add a method to a path in a map. <p>
     *
     * Methods are passed in using "methodName", meaning they must be a member of
     * the current handler.
     *
     * @param httpMethod    The HTTP method this route will match to.
     * @param path	    Path to match.
     * @param route	    The Route to be called at said path.
     *
     * @throws HttpException  When you do bad things.
     */
    public void addRoute(String httpMethod, Route route) {
        httpMethod = httpMethod.toUpperCase();

        if (!routes.containsKey(httpMethod)) {
            routes.put(httpMethod, new ArrayList<>());
        }

        routes.get(httpMethod).add(route);
    }



    /******************************
      Generic getters and setters
     ******************************/

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public Socket getSocket() {
        return socket;
    }

    public void setWriter(DataOutputStream writer) {
        this.writer = writer;
    }
    public DataOutputStream getWriter() {
        return writer;
    }
}

package httpserver;

import java.util.HashMap;
import java.util.Map;

/**
 * An HttpRouter is used to route incoming requests to specific handlers.
 *
 * @see HttpHandler
 * @see HttpRequest
 */
public class HttpRouter {
    private Map<String, HttpHandler> handlers;
    private HttpHandler errorHandler;
    private HttpHandler defaultHandler;

    public HttpRouter() {
        handlers = new HashMap<>();
        errorHandler = new DeathHandler(501);
        defaultHandler = null;
    };


    /**
     * Route determines which {@link HttpHandler} to use based on the first path
     * segment (between the first and second `/`). <p>
     *
     * If no {@link HttpHandler} can be found for the specified path segment, an
     * error handler is used. You can specify a specific error handler using the
     * {@link #setErrorHandler(HttpHandler)} method. The default error handler
     * will send a `501` status code (Not Implemented) to the client.
     *
     * @see HttpHandler
     */
    public HttpHandler route(String pathSegment, HttpRequest request) {

        if (getHandlers().containsKey(pathSegment)) {
            request.setPath(request.getPath().substring(pathSegment.length() + 1));
            return getHandlers().get(pathSegment);
        } else if (defaultHandler != null) {
            return defaultHandler;
        }

        return getErrorHandler();
    }


    /**
     * Get the map used to route paths to specific handlers
     * @return The router's map of path segments and handlers.
     */
    public Map<String, HttpHandler> getHandlers() {
        return handlers;
    }


    /**
     * Add a new route.
     *
     * @param pathSegment     The first path segment (
     *                        between the first and second {@code /}) to match
     * @param handler         An HttpHandler to be routed to.
     */
    public void addHandler(String pathSegment, HttpHandler handler) {
        getHandlers().put(pathSegment, handler);
    }


    public void setErrorHandler(HttpHandler handler) {
        errorHandler = handler;
    }
    public HttpHandler getErrorHandler() {
        return errorHandler;
    }

    public void setDefaultHandler(HttpHandler handler) {
        defaultHandler = handler;
    }
    public HttpHandler getDefaultHandler() {
        return defaultHandler;
    }
}

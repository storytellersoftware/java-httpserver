package httpserver;

import java.util.HashMap;
import java.util.Map;

/**
 * An HTTPRouter is used to route incoming requests to specific handlers. 
 *
 * @see HTTPHandler
 * @see HTTPRequest
 */
public class HTTPRouter {
  private Map<String, HTTPHandler> handlers;
  private HTTPHandler errorHandler;
  private HTTPHandler defaultHandler;

  public HTTPRouter() {
    handlers = new HashMap<>();
    try {
      errorHandler = new DeathHandler(501);
    } catch (HTTPException e) {
      throw new RuntimeException(
        "DeathHandler threw an HTTPException. Something really, really bad has happened.");
    }
    
    defaultHandler = null;
  };


  /**
   * Route determines which {@link HTTPHandler} to use based on the first path
   * segment (between the first and second `/`). <p>
   * 
   * If no {@link HTTPHandler} can be found for the specified path segment, an
   * error handler is used. You can specify a specific error handler using the
   * {@link #setErrorHandler(HTTPHandler)} method. The default error handler
   * will send a `501` status code (Not Implemented) to the client.
   * 
   * @see HTTPHandler
   */
  public HTTPHandler route(String pathSegment, HTTPRequest request) 
      throws HTTPException {
    
    if (getHandlers().containsKey(pathSegment)) {
      request.setPath(request.getPath().substring(pathSegment.length() + 1));
      return getHandlers().get(pathSegment);
    }
    else if (defaultHandler != null) {
      return defaultHandler;
    }

    return getErrorHandler();
  }


  /**
   * Get the map used to route paths to specific handlers
   * @return The router's map of path segments and handlers.
   */
  public Map<String, HTTPHandler> getHandlers() {
    return handlers;
  }

  
  /**
   * Add a new route.
   * 
   * @param pathSegment     The first path segment (
   *                        between the first and second {@code /) to match
   * @param handler         An HTTPHandler to be routed to.
   */
  public void addHandler(String pathSegment, HTTPHandler handler) {
    getHandlers().put(pathSegment, handler);
  }
  
  
  public void setErrorHandler(HTTPHandler handler) {
    errorHandler = handler;
  }
  public HTTPHandler getErrorHandler() {
    return errorHandler;
  }
  
  public void setDefaultHandler(HTTPHandler handler) {
    defaultHandler = handler;
  }
  public HTTPHandler getDefaultHandler() {
    return defaultHandler;
  }
}
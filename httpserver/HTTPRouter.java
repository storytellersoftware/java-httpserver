package httpserver;

import java.util.HashMap;
import java.util.Map;

/**
 * An HTTPHandlerFactory is a factory that's used to determine what kind of
 * HTTPHandler should be used in the HTTPRequest.
 *
 * @see HTTPHandler
 * @see HTTPRequest
 */
public class HTTPRouter {
  private Map<String, HTTPHandler> handlers;

  public HTTPRouter() {
    handlers = new HashMap<>();
  };


  /**
   * Figures out what kind of HTTPHandler should be used to set the response
   * data.
   */
  public HTTPHandler determineHandler(String pathSegment,
          HTTPRequest request) throws HTTPException {

    if (getHandlers().containsKey(pathSegment)) {
      String path = request.getPath();
      request.setPath(path.substring(path.indexOf(pathSegment) + pathSegment.length()));
      return getHandlers().get(pathSegment);
    }
    else if (getHandlers().containsKey("*")) {
      return getHandlers().get("*");
    }

    return new DeathHandler();
  }


  public Map<String, HTTPHandler> getHandlers() {
    return handlers;
  }

  public void addHandler(String pathSegment, HTTPHandler handler) {
    getHandlers().put(pathSegment, handler);
  }

  /**
   * Check if the pathSegment is equal to the key.
   * Removes the pathSegment from the request's path
   * ({@code HTTPRequest.getPath()})
   *
   * @param pathSegment The first part of the URL.
   * @param key The key to check against.
   * @param request The request given by the client.
   *
   * @return Whether the path segment is equal to the key.
   */
  public boolean checkIfEquals(String pathSegment, String key,
          HTTPRequest request) {
    String path = request.getPath();
    request.setPath(path.substring(path.indexOf(key) + 1));

    return pathSegment.equalsIgnoreCase(key);
  }
}
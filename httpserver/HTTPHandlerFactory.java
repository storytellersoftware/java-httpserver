package httpserver;

/**
 * An HTTPHandlerFactory is a factory that's used to determine what kind of
 * HTTPHandler should be used in the HTTPRequest.
 *
 * @see HTTPHandler
 * @see HTTPRequest
 */
public abstract class HTTPHandlerFactory {
  public HTTPHandlerFactory() {};


  /**
   * Figures out what kind of HTTPHandler should be used to set the response
   * data.
   */
  public abstract HTTPHandler determineHandler(String pathSegment,
      HTTPRequest request) throws HTTPException;

  /**
   * Check if the pathSegment is equal to the key.
   * Removes the pathSegment from the request's path ({@code HTTPRequest.getPath()})
   * @param pathSegment
   * @param key
   * @param request
   * @return
   */
  public boolean checkIfEquals(String pathSegment, String key, HTTPRequest request) {
    String path = request.getPath();
    request.setPath(path.substring(path.indexOf(key) + 1));

    return pathSegment.equalsIgnoreCase(key);
  }

}
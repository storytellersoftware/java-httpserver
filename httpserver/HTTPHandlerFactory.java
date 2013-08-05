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

}
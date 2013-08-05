package httpserver;

/**
 * An HTTPHandlerFactory is a singleton that's used to determine what kind of
 * HTTPHandler should be used in the HTTPRequest.
 *
 * @see HTTPHandler
 * @see HTTPRequest
 */
public abstract class HTTPHandlerFactory {
  private static HTTPHandlerFactory instance;
  private static Class<? extends HTTPHandlerFactory> factoryClass 
          = HTTPHandlerFactory.class;

  private HTTPHandlerFactory() {};


  /**
   * Return the HTTPHandlerFactory's instance.
   *
   * Because HTTPHandlerFactory is a singleton, there is only one 
   * HTTPHandlerFactory. When you want to access it, you use this.
   *
   * @return  The one and only HTTPHandlerFactory out there.
   */
  public static HTTPHandlerFactory getInstance() {
    if (instance == null) {
      try {
        instance = factoryClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }


  /**
   * Figures out what kind of HTTPHandler should be used to set the response
   * data.
   */
  public abstract HTTPHandler determineHandler(String pathSegment, 
        HTTPRequest request);

}
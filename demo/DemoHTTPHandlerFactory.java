package demo;

import httpserver.*;

public class DemoHTTPHandlerFactory extends HTTPHandlerFactory {
  public DemoHTTPHandlerFactory() {}


  @Override
  public HTTPHandler determineHandler(String pathSegment,
      HTTPRequest request) throws HTTPException {
    if (pathSegment.equalsIgnoreCase("hello"))
      return new HelloHandler(request);

    if (pathSegment.equalsIgnoreCase("math"))
      return new MathHandler(request);

    return new MathHandler(request);
  }
}
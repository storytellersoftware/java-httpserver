package demo;

import httpserver.*;

public class DemoHTTPHandlerFactory extends HTTPHandlerFactory {
  public DemoHTTPHandlerFactory() {}


  public HTTPHandler determineHandler(String pathSegment, 
          HTTPRequest request) throws HTTPException {
    if (pathSegment.equalsIgnoreCase("hello")) {
      return new HelloHandler(request);
    }

    return new MathHandler(request);
  }
}
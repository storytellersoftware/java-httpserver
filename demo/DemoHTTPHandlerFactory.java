package demo;

import httpserver.*;

public class DemoHTTPHandlerFactory extends HTTPHandlerFactory {
  private static Class<? extends HTTPHandlerFactory> factoryClass
          = DemoHTTPHandlerFactory.class;

  private DemoHTTPHandlerFactory() {}


  public HTTPHandler determineHandler(String pathSegment, 
          HTTPRequest request) {
    if (pathSegment.equalsIgnoreCase("hello")) {
      return new HelloHandler(request);
    }

    return new MathHandler(request);
  }
}
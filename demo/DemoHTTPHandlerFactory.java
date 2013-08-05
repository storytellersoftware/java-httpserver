package demo;

import httpserver.*;

public class DemoHTTPHandlerFactory extends HTTPHandlerFactory {
  public DemoHTTPHandlerFactory() {}


  @Override
  public HTTPHandler determineHandler(String pathSegment,
      HTTPRequest request) throws HTTPException {

    if (checkIfEquals(pathSegment, "hello", request))
      return new HelloHandler(request);

    if (checkIfEquals(pathSegment, "math", request))
      return new MathHandler(request);

    return new MathHandler(request);
  }
}
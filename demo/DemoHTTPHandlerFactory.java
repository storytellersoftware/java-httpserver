package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPHandlerFactory;
import httpserver.HTTPRequest;

public class DemoHTTPHandlerFactory extends HTTPHandlerFactory {
  public DemoHTTPHandlerFactory() {}


  @Override
  public HTTPHandler determineHandler(String pathSegment, HTTPRequest request)
          throws HTTPException {

    if (checkIfEquals(pathSegment, "hello", request))
      return new HelloHandler(request);

    if (checkIfEquals(pathSegment, "math", request))
      return new MathHandler(request);

    if(checkIfEquals(pathSegment, "mathtest", request))
      return new MathArrayHandler(request);

    return new FileHandler(request);
  }
}
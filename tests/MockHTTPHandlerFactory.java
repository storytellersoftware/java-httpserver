package tests;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPHandlerFactory;
import httpserver.HTTPRequest;
import httpserver.MessageHandler;

public class MockHTTPHandlerFactory extends HTTPHandlerFactory {

  public MockHTTPHandlerFactory() {}

  @Override
  public HTTPHandler determineHandler(String pathSegment, HTTPRequest request)
          throws HTTPException {
    return new MessageHandler(request, "Hello World!");
  }

}

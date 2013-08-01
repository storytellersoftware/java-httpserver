package handlers;

import httpserver.*;

public class HelloHandler extends HTTPHandler {

  public HelloHandler(HTTPRequest request) {
    super(request);
  }

  public void handle() {
    message(200, "Hello, World!");
  }
}
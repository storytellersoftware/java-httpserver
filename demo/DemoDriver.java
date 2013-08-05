package demo;

import httpserver.*;

public class DemoDriver {
  public static void main(String[] args) {
    HTTPServer s = new HTTPServer();
    s.setHandlerFactory(DemoHTTPHandlerFactory.getInstance());

    s.run();
  }
}
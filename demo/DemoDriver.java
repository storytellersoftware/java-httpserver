package demo;

import httpserver.*;

/**
 * This is where everything is started.<p>
 * The server is created and ran here, and that's about it.
 *
 */
public class DemoDriver {
  /**
   * Start the server.
   * @param args Nothing.
   */
  public static void main(String[] args) {
    HTTPServer s = new HTTPServer();
    s.setHandlerFactory(new DemoHTTPHandlerFactory());

    s.run();
  }
}
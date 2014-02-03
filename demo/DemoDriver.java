package demo;

import httpserver.HTTPHandlerFactory;
import httpserver.HTTPServer;

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
    HTTPHandlerFactory f = new HTTPHandlerFactory();
    f.addHandler("hello", new HelloHandler());
    f.addHandler("math", new MathHandler());
    f.addHandler("matharray", new MathArrayHandler());
    f.addHandler("*", new FileHandler());

    s.setHandlerFactory(f);

    s.run();
  }
}
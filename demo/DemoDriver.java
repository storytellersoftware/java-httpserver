package demo;

import httpserver.HTTPException;
import httpserver.HTTPRouter;
import httpserver.HTTPServer;
import tests.HandlerTest;

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
    try {
      HTTPServer s = new HTTPServer();
      HTTPRouter f = new HTTPRouter();
      f.addHandler("hello", new HelloHandler());
      f.addHandler("math", new MathHandler());
      f.addHandler("matharray", new MathArrayHandler());
      f.addHandler("test", new HandlerTest());
      f.addHandler("*", new FileHandler());
  
      s.setHandlerFactory(f);
  
      s.run();
    } catch (HTTPException e) {
      System.out.println("Server encountered an exception...");
      e.printStackTrace();
    }
  }
}
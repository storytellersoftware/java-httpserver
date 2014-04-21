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
      HTTPRouter r = new HTTPRouter();
      r.addHandler("hello", new HelloHandler());
      r.addHandler("math", new MathHandler());
      r.addHandler("matharray", new MathArrayHandler());
      r.addHandler("test", new HandlerTest());
      r.setDefaultHandler(new FileHandler());
  
      s.setRouter(r);
  
      s.run();
    } catch (HTTPException e) {
      System.out.println("Server encountered an exception...");
      e.printStackTrace();
    }
  }
}
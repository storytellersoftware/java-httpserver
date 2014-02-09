package tests;

import static org.junit.Assert.fail;
import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;
import httpserver.HTTPResponse;
import httpserver.HTTPRouter;

import org.junit.Test;

public class HandlerTest extends HTTPHandler {
  public HandlerTest() throws HTTPException {
    addGET("/showHeaders", "showHeaders");
    addGET("/hello", "sayHello");
    addGET("/hello/{String}", "sayHello");
    addGET("/hello/{String}/{String}", "sayHello");
    
    addGET("/thisAndMore/{String...}", "thisAndMore");
  }
  
  public void showHeaders(HTTPResponse resp, HTTPRequest req) {
    StringBuilder b = new StringBuilder();
    for (String key: req.getHeaders().keySet()) {
      b.append(key);
      b.append(":\t");
      b.append(req.getHeaders().get(key));
      b.append("\n\n");
    }
    
    resp.setBody("Headers: \n\n" + b.toString());
  }
  
  public void sayHello(HTTPResponse resp) {
    resp.setBody("Hello World!");
  }
  
  public void sayHello(HTTPResponse resp, String name) {
    resp.setBody("Hello " + name + "!");
  }
  
  public void sayHello(HTTPResponse resp, String first, String last) {
    resp.setBody("Hello " + first + " " + last + "!");
  }
  
  public void thisAndMore(HTTPResponse resp, String... paths) {
    StringBuilder b = new StringBuilder("You requested `");
    for (String p: paths) {
      b.append("/");
      b.append(p);
    }
    b.append("`");
    
    resp.setBody(b.toString());
  }
  
  
  @Test
  public void testHandlerCreation() {
    try {
      HTTPRouter f = new HTTPRouter();
      f.addHandler("/", new HandlerTest());
    } catch (HTTPException e) {
      e.printStackTrace();
      fail("Couldn't create new handler...");
    }
  }

}

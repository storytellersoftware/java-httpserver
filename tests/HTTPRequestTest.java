package tests;

import static org.junit.Assert.*;
import httpserver.*;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Test;


public class HTTPRequestTest {
  
  private static ServerSocket server;

  @Before
  public void setUp() throws IOException {
    HTTPRequest.setHandlerFactory(new MockHTTPHandlerFactory());
    
    server = new ServerSocket(MockClient.DESIRED_PORT);
  }
  
  @Test
  public void simpleGETRequest() {
    try {
      MockClient c = new MockClient();
      c.fillInSocket();
      HTTPRequest r = new HTTPRequest(server.accept());
      
      
      assertEquals(c.getRequestType(), r.getRequestType());
      assertEquals(c.getPath(), r.getPath());
      assertEquals(c.getGetData(), r.getGetData());
      assertEquals(c.getHeaders(), r.getHeaders());
      assertEquals(c.getPostData(), r.getPostData());
    }
    catch (HTTPException | IOException e) {
      e.printStackTrace();
      fail("Exception occured...");
    }
  }
  
  

}



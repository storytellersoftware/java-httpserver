package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import httpserver.HTTPException;
import httpserver.HTTPRequest;
import httpserver.HTTPRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import demo.MathArrayHandler;


public class HTTPRequestTest {
  
  private static ServerSocket server;
  
  @BeforeClass
  public static void init() throws IOException, HTTPException {
    HTTPRouter f = new HTTPRouter();
    f.addHandler("math", new MathArrayHandler());
	HTTPRequest.setRouter(f);
    
    server = new ServerSocket(MockClient.DESIRED_PORT);
  }
  
  @AfterClass
  public static void deinit() throws IOException {
    server.close();
  }
  
  @Test
  public void simpleGETRequest() {
    try {
      MockClient c = new MockClient();
      c.fillInSocket();
      HTTPRequest r = new HTTPRequest(server.accept());
      r.parseRequest();
      
      
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
  
  @Test
  public void simplePOSTRequest() {
    HashMap<String, String> data = new HashMap<String, String>();
    
    data.put("name", "don");
    data.put("a", "b");
    data.put("c", "d");
    
    try {
      MockClient c = new MockClient();
      c.getPostData().putAll(data);
      c.setRequestType("POST");
      
      c.fillInSocket();
      HTTPRequest r = new HTTPRequest(server.accept());
      r.parseRequest();
      
      assertEquals(c.getRequestType(), r.getRequestType());
      assertEquals(c.getPostData(), r.getPostData());
      assertEquals(c.getHeaders(), r.getHeaders());
    }
    catch (HTTPException | IOException e) {
      e.printStackTrace();
      fail("Exception occured...");
    }
  }
  
  @Test
  public void POSTRequestWithGETData() {
    HashMap<String, String> getData = new HashMap<String, String>();
    getData.put("dysomnia-1", "Io");
    getData.put("dysomnia-2", "Sinope");
    getData.put("dysomnia-3", "Atlas");
    getData.put("dysomnia-4", "Nix");
    
    
    HashMap<String, String> postData = new HashMap<String, String>();
    postData.put("dysomnia-5", "Moon");
    postData.put("dysomina-6", "Ymir");
    postData.put("dysomnia-7", "Ijiraq");
    postData.put("dysomnia-8", "Algol");
    
    
    try {
      MockClient c = new MockClient();
      c.setGetData(getData);
      c.setPostData(postData);
      c.setPath("/path/to/request/file.html");
      c.setRequestType("POST");
      
      c.fillInSocket();
      HTTPRequest r = new HTTPRequest(server.accept());
      r.parseRequest();
      
      assertEquals(c.getGetData(), r.getGetData());
      assertEquals(c.getPostData(), r.getPostData());
      assertEquals(c.getPathWithGetData(), r.getPath());
      
    }
    catch (HTTPException | IOException e) {
      e.printStackTrace();
      fail("Exception occured...");
    }
  }
  
}



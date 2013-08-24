package tests;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MockClient {
  
  public static int DESIRED_PORT = 4444;
  
  
  public String requestType;
  public String path;
  public String protocol;
  
  public Map<String, String> getData;
  public Map<String, String> postData;
  public Map<String, String> headers;
  
  
  public MockClient() {
    setDefault();
  }
  
  
  public void setDefault() {
    setRequestType("GET");
    setPath("/");
    setProtocol("HTTP/1.1");
    
    setGetData(new HashMap<String, String>());
    setPostData(new HashMap<String, String>());
    setHeaders(new HashMap<String, String>());
  }
  
  public void fillInSocket() throws IOException {
    //ServerSocket s = new ServerSocket(4444);
    Socket socket = new Socket("127.0.0.1", DESIRED_PORT);
    BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream()));
    
    writer.write(getRequestLine());
    writer.write("\n");
    
    if (!getPostData().isEmpty()) {
      getHeaders().put("Content-Length", getDataInHTTP(getPostData()));
    }
    
    writer.write(getHeadersInHTTP());
    writer.write("\n");
    
    writer.write(getDataInHTTP(getPostData()));
    writer.flush();
    writer.close();
    socket.close();
    
    //return s.accept();
  }
  
  public String getRequestLine() {
    StringBuilder b = new StringBuilder();
    b.append(getRequestType());
    b.append(" ");
    b.append(getPathWithGetData());
    b.append(" ");
    b.append(getProtocol());
    
    return b.toString();
  }
  
  public String getHeadersInHTTP() {
    StringBuilder b = new StringBuilder();
    
    for (String key : getHeaders().keySet()) {
      String value = getHeaders().get(key);
      
      b.append(key.replace(" ", "-"));
      b.append(": ");
      b.append(value.replace(" ", "-"));
      b.append("\n");
    }
    
    return b.toString();
  }
  
  public String getDataInHTTP(Map<String, String> data) {
    StringBuilder b = new StringBuilder();
    
    for (String key : data.keySet()) {
      String value = data.get(key);
      
      try {
        key = URLEncoder.encode(key, "UTF-8");
        value = URLEncoder.encode(value, "UTF-8");
      } 
      catch (UnsupportedEncodingException e) {
        e.printStackTrace(); // TODO remove when done testing
      }
      
      b.append(key);
      b.append("=");
      b.append(value);
      b.append("&");
    }
    
    if (b.length() != 0) {
      b.deleteCharAt(b.length() - 1);
    }
    
    return b.toString();
  }
  
  public String getPathWithGetData() {
    StringBuilder b = new StringBuilder();
    b.append(getPath());
    
    if (!getGetData().isEmpty()) {
      b.append("?");
      b.append(getDataInHTTP(getGetData()));
    }
    
    return b.toString();
  }
  
  // -------------------
  // Getters and Setters
  // -------------------
  
  public String getRequestType() {
    return requestType;
  }
  public void setRequestType(String requestType) {
    this.requestType = requestType.toUpperCase();
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public String getProtocol() {
    return protocol;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }
  public Map<String, String> getGetData() {
    return getData;
  }
  public void setGetData(Map<String, String> getData) {
    this.getData = getData;
  }
  public Map<String, String> getPostData() {
    return postData;
  }
  public void setPostData(Map<String, String> postData) {
    this.postData = postData;
  }
  public Map<String, String> getHeaders() {
    return headers;
  }
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }
  
}

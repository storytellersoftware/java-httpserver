package httpserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {
  /** Generic error message for when an exception occurs on the server */
  public static final String EXCEPTION_ERROR
          = "an exception occured while processing your request";

  /** Generic error message for when there isn't a method assigned to the
          requested path */
  public static final String NOT_A_METHOD_ERROR = "No known method";

  /** Generic error message for when the browser sends bad data */
  public static final String MALFORMED_INPUT_ERROR = "Malformed Input";

  /** Generic status message for when everything is good */
  public static final String STATUS_GOOD = "All systems are go";
  
  private static String serverInfo;
  private static Map<Integer, String> responses;
  
  private HTTPRequest request;
  
  private int code;
  private String body;
  private String mimeType;
  private long size;
  
  private Map<String, String> headers;
  
  private Socket socket;
  private DataOutputStream writer;
  
  public HTTPResponse(HTTPRequest req) throws IOException {
    if (getServerInfo() == null || getServerInfo().isEmpty()) {
      setupServerInfo();
    }
    
    socket = req.getConnection();
    writer = new DataOutputStream(socket.getOutputStream());
    
    request = req; 
    headers = new HashMap<>();
    setCode(200); // 200 OK
    setSize(-1);
    setMimeType("text/plain");
  }
  

  /**
   * Send a simple string message with an HTTP response code back to
   * the client. <p>
   *
   * Can be used for sending all data back.
   *
   * @param code      An HTTP response code.
   * @param message   The content of the server's response to the browser
   *
   * @see HTTPHandler#error
   * @see HTTPHandler#noContent
   */
  public void message(int code, String message) {
    setCode(code);
    setBody(message);
  }
  
  
  /**
   * Tell the browser there is no response data. <p>
   *
   * This is done by sending over a 204 code, which means there isn't
   * any data in the stream, but the server correctly processed the request
   *
   * @see HTTPHandler#message
   */
  public void noContent() {
    setCode(204);
    setBody("");
  }
  
  
  /**
   * Send a message to the browser and print an exception<p>
   *
   * Prints the stackTrace of `t`, and sends a message `message` back to the
   * browser, with that HTTP status of `code`
   * 
   * @param code      HTTP status code
   * @param message   the content being sent back to the browser
   * @param t         A throwable object, to be printed to the screen
   * 
   * @see httpserver.HTTPHandler#message
   */
  public void error(int code, String message, Throwable t) {
    t.printStackTrace();
    message(code, message);
  }
  
  
  /**
   * Send data back to the client. <p>
   *
   * If the response hasn't been handled, try handling it, and sending
   * that data back to the client. <p>
   *
   * This method only writes the headers, and calls
   * {@link HTTPHandler#writeData()} to send the determined response back to
   * the client. This is done because the headers are fairly global, and
   * shouldn't need to be changed, where the actual response data might need
   * some "special sauce".
   *
   * @see HTTPHandler#writeData
   */
  public void respond() {
    try {
      if (getSocket() == null)
        throw new HTTPException("Socket is null...");
      else if (getSocket().isClosed())
        throw new HTTPException("Socket is closed...");
      
  
      if(getBody() == null) {
        noContent();
      }

      writeLine("HTTP/1.1 " + getResponseCodeMessage(getCode()));
      writeLine("Server: " + getServerInfo());
      writeLine("Content-Type: " + getMimeType());

      writeLine("Connection: close");

      if (getSize() != -1) {
        writeLine("Content-Size: " + getSize());
      }
      else {
        writeLine("Content-Size: " + getBody().length());
      }

      if (!getHeaders().isEmpty()) {
        for (String key : getHeaders().keySet()) {
          StringBuilder b = new StringBuilder();
          b.append(key);
          b.append(": ");
          b.append(getHeader(key));

          writeLine(b.toString());
        }
      }

      writeLine("");

      if (getRequest().isType(HTTPRequest.HEAD_REQUEST_TYPE)
              || getCode() == 204) {
        return;
      }

      getWriter().writeBytes(getBody());
    }
    catch (HTTPException | IOException e) {
      System.err.println("Something bad happened while trying to send data "
              + "to the client");
      e.printStackTrace();
    }
    finally {
      try {
        getWriter().close();
      }
      catch (NullPointerException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Writes a string and a "\n" to the DataOutputStream.
   * @param line The line to write
   * @throws IOException
   */
  protected void writeLine(String line) throws IOException {
    getWriter().writeBytes(line + "\n");
  }
  
  
  /**
   * Return the response code + the response message.
   *
   * @see HTTPHandler#getResponseCode
   * @see HTTPHandler#setResponseCode
   */
  public static String getResponseCodeMessage(int code) {
    if (responses == null || responses.isEmpty()) {
      setupResponses();
    }

    if (responses.containsKey(code)) {
      return code + " " + responses.get(code);
    }

    return Integer.toString(code);
  }
  
  /**
   * Sets up a list of response codes and text.
   */
  private static void setupResponses() {
    responses = new HashMap<Integer, String>();

    responses.put(100, "Continue");
    responses.put(101, "Switching Protocols");

    responses.put(200, "OK");
    responses.put(201, "Created");
    responses.put(202, "Accepted");
    responses.put(203, "Non-Authoritative Information");
    responses.put(204, "No Content");
    responses.put(205, "Reset Content");
    responses.put(206, "Partial Content");

    responses.put(300, "Multiple Choices");
    responses.put(301, "Moved Permanently");
    responses.put(302, "Found");
    responses.put(303, "See Other");
    responses.put(304, "Not Modified");
    responses.put(305, "Use Proxy");
    responses.put(307, "Temporary Redirect");

    responses.put(400, "Bad Request");
    responses.put(401, "Unauthorized");
    responses.put(402, "Payment Required");
    responses.put(403, "Forbidden");
    responses.put(404, "Not Found");
    responses.put(405, "Method Not Allowed");
    responses.put(406, "Not Acceptable");
    responses.put(407, "Proxy Authentication Required");
    responses.put(408, "Request Timeout");
    responses.put(409, "Conflict");
    responses.put(410, "Gone");
    responses.put(411, "Length Required");
    responses.put(412, "Precondition Failed");
    responses.put(413, "Request Entity Too Large");
    responses.put(414, "Request-URI Too Long");
    responses.put(415, "Unsupported Media Type");
    responses.put(416, "Request Range Not Satisfiable");
    responses.put(417, "Expectation Failed");
    responses.put(418, "I'm a teapot");
    responses.put(420, "Enhance Your Calm");

    responses.put(500, "Internal Server Error");
    responses.put(501, "Not implemented");
    responses.put(502, "Bad Gateway");
    responses.put(503, "Service Unavaliable");
    responses.put(504, "Gateway Timeout");
    responses.put(505, "HTTP Version Not Supported");
  }

  
  
  
  public int getCode() {
    return code;
  }
  public void setCode(int code) {
    this.code = code;
  }


  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }


  public String getMimeType() {
    return mimeType;
  }
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }


  public long getSize() {
    return size;
  }
  public void setSize(long size) {
    this.size = size;
  }


  public Map<String, String> getHeaders() {
    return headers;
  }
  public String getHeader(String key) {
    return headers.get(key);
  }
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }
  public void setHeader(String key, String value) {
    this.headers.put(key, value);
  }


  private HTTPRequest getRequest() {
    return request;
  }


  private Socket getSocket() {
    return socket;
  }
  private DataOutputStream getWriter() {
    return writer;
  }



  /**
   * Set the info of the server
   */
  public void setupServerInfo() {
    StringBuilder info = new StringBuilder();
    info.append(HTTPServer.getServerName());
    info.append(" v");
    info.append(HTTPServer.getServerVersion());
    info.append(" (");
    info.append(HTTPServer.getServerETC());
    info.append(")");
    setServerInfo(info.toString());
  }
  public static void setServerInfo(String serverInfo) {
    HTTPResponse.serverInfo = serverInfo;
  }
  public static String getServerInfo() {
    return serverInfo;
  }
}

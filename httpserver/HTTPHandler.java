package httpserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An HTTPHandler is what all handlers used by your server descend from. <p>
 *
 * HTTPHandlers also deal with the server -> client transmission of data,
 * making it easy to adjust how data is sent to the client. <p>
 * 
 * Extended classes have two options for determining their actions: they may
 * override the handle method (slightly harder), or use the addGet and addPost
 * methods in the constructor. See their descriptions for more information. <p>
 * 
 * If you just want to send a static message to the client, regardless of
 * request, you can use a MessageHandler, instead of creating a new Handler.
 *
 * @see HTTPHandler#handle
 * @see HTTPHandler#addGET
 * @see HTTPHandler#addPOST
 */
public abstract class HTTPHandler {
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

  private final HashMap<String, MethodWrapper> getMethods
  = new HashMap<String, MethodWrapper>();
  private final HashMap<String, MethodWrapper> postMethods
  = new HashMap<String, MethodWrapper>();
  private final HashMap<String, MethodWrapper> deleteMethods
  = new HashMap<String, MethodWrapper>();
  private final HashMap<String, MethodWrapper> putMethods
  = new HashMap<String, MethodWrapper>();

  private HTTPRequest request;
  private int responseCode;
  private String responseType;
  private String responseText;
  private long responseSize;
  private boolean handled;

  private Socket socket;
  private DataOutputStream writer;
  private Map<Integer, String> responses;

  /**
   * Create an HTTPHandler. <p>
   * 
   * This is where paths should be setup<p>
   *
   * This also sets some acceptable defaults: <ul>
   *    <li>The response code is set to 200 (OK, which means everything happend
   *    all nice and good-like);
   *
   *    <li>The response size is set to -1, which tells the HTTPResponse to
   *    determine the correct size when sends back the information;
   *
   *    <li>The response is told it hasn't been handled yet;
   *
   *    <li>And the response mimetype is set to "text/plain".
   *
   * @param request HTTPRequest with the browser's request information.
   *
   * @see HTTPRequest
   * @see HTTPHandler#setResponseCode
   * @see HTTPHandler#setResponseSize
   * @see HTTPHandler#setHandled
   * @see HTTPHandler#setResponseType
   */
  public HTTPHandler(HTTPRequest request) throws HTTPException {
    try {
      setSocket(request.getConnection());
      setRequest(request);
      setWriter(new DataOutputStream(getSocket().getOutputStream()));
    }
    catch (IOException e) {
      throw new HTTPException("IOException...", e);
    }

    if (getServerInfo() == null || getServerInfo().isEmpty()) {
      setupServerInfo();
    }

    setResponseCode(200);
    setResponseSize(-1);
    setHandled(false);
    setResponseType("text/plain");
  }


  /**
   * Where the Handler handles the information given from the request and
   * based off of the paths specified in the Handler. <p>
   *
   * This can be overridden for more fine-grained handling. As is, it uses
   * the data behind the addGET and addPOST methods for determining the
   * correct action to take. <p>
   *
   * If there is not exact match, the `*` and `/` path's are used, in that
   * order. If, after that, no method can be found, a 501 is sent over to the
   * client, with the <code>NOT_A_METHOD_ERROR</code> message.
   *
   * @throws HTTPException  when an attached method can't be invoked.
   *
   * @see HTTPHandler#addGET
   * @see HTTPHandler#addPOST
   * @see HTTPHandler#NOT_A_METHOD_ERROR
   */
  public void handle() throws HTTPException {
    String path = getRequest().getPath();
    MethodWrapper method = getMap().get(path);

    /*  If the above MethodWrapper is null (occurs when the requested path
        is dynamic), find the best fit method based on MethodWrapper's scoring
        technique (see MethodWrapper#howCorrect for more information).
     */
    if (method == null) {
      int bestFit = 0;
      Set<String> keys = getMap().keySet();
      for (String key : keys) {
        MethodWrapper testMethod = getMap().get(key);
        int testScore = testMethod.howCorrect(path);

        if (testScore > bestFit) {
          method = testMethod;
          bestFit = testScore;
        }
      }
    }

    /*  If none of the paths match the request's path, try using a wild-card
        or root path in that order.
     */
    if (method == null) {
      if (getMap().containsKey("*")) {
        method = getMap().get("*");
      }
      else if (getMap().containsKey("/")) {
        method = getMap().get("/");
      }
    }

    /*  If, following the whole ordeal, no acceptable method is found, send the
        client a 501, Not a Method error.
     */
    if (method == null) {
      message(501, NOT_A_METHOD_ERROR);
      return;
    }

    System.out.println("Method Invoked: " + method);
    method.invoke(this, path);
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
    setResponseCode(code);
    setResponseText(message);
    setHandled(true);
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
    setResponseCode(204);
    setResponseText("");
    setHandled(true);
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
   * Gets the correct Map of Methods the request wants to use.
   * @return  The HashMap for the correct request. Defaults to GET if
   *          the method isn't known.
   */
  private HashMap<String, MethodWrapper> getMap() {
    if(getRequest().isType(HTTPRequest.POST_REQUEST_TYPE)) {
      return postMethods;
    }
    if(getRequest().isType(HTTPRequest.DELETE_REQUEST_TYPE)) {
      return deleteMethods;
    }

    return getMethods;
  }

  /**
   * Attach a method to a GET request at a path. <p>
   *
   * Methods are passed in using "className#methodName" form so that
   * we can parse out the correct Method. Who knows, you might want to
   * use a method somewhere else, and who are we to argue with that? <p>
   *
   * If no # is included, we assume it belongs to the class it's called in. <p>
   *
   * Path's should come in "/path/to/action" form. If the method requires
   * parameters, they should be included in the path, in the order they're
   * listed in the method definition, but in "{ClassName}" form. Example:
   * <code>/hello/{String}/{String}</code> is a good path. <p>
   *
   * Methods being used should <strong>only</strong> have parameters that are
   * included in the java.lang library. Any other type of parameter will cause
   * an exception to occur. <p>
   *
   * Additionally, primitives are not permited, because they're not classes in
   * the java.lang library.
   *
   * @param path        Path to match
   * @param methodName  Class and Method in class#method form.
   * @throws HTTPException When you do bad things.
   *
   * @see HTTPHandler#addPOST
   * @see HTTPHandler#addDELETE
   */
  public void addGET(String path, String methodName) throws HTTPException {
    addMethod(getMethods, path, methodName);
  }

  /**
   * Attach a method to a POST request at a path. <p>
   *
   * For a more detailed explanation, see {@link HTTPHandler#addGET}.
   *
   * @param path         Path to match
   * @param methodName   Class and Method in class#method form.
   * @throws HTTPException When you do bad things.
   *
   * @see HTTPHandler#addGET
   * @see HTTPHandler#addDELETE
   */
  public void addPOST(String path, String methodName) throws HTTPException {
    addMethod(postMethods, path, methodName);
  }

  /**
   * Attach a method to a DELETE request at a path. <p>
   *
   * For a more detailed explanation, see {@link HTTPHandler#addGET}.
   *
   * @param path        Path to match
   * @param methodName  Class and Method in class#method form.
   * @throws HTTPException when you do bad things.
   *
   * @see HTTPHandler#addGET
   * @see HTTPHandler#addPOST
   */
  public void addDELETE(String path, String methodName) throws HTTPException {
    addMethod(deleteMethods, path, methodName);
  }

  /**
   * Add a method to a path in a map. <p>
   *
   * Methods are passed in using "className#methodName" form so that
   * we can parse out the correct Method. Who knows, you might want to
   * use a method somewhere else, and who are we to argue with that? <p>
   *
   * If no # is included, we assume it belongs to the class it's called in.
   *
   * @param map             The map to add this junks to.
   * @param path            Path to match
   * @param classMethod     Class and Method in class#method form.
   *
   * @throws HTTPException  When you do bad things.
   */
  private void addMethod(HashMap<String, MethodWrapper> map, String path,
          String methodName) throws HTTPException {

    MethodWrapper method = new MethodWrapper(path, methodName, getClass());
    map.put(path, method);
  }

  /***************************
    Response-specific things
   ***************************/

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
      if (!isHandled()) {
        handle();
      }

      if(getResponseText() == null) {
        noContent();
      }

      writeLine("HTTP/1.1 " + getResponseCodeMessage());
      writeLine("Server: " + getServerInfo());
      writeLine("Content-Type: " + getResponseType());

      writeLine("Connection: close");

      if (getResponseSize() != -1) {
        writeLine("Content-Size: " + getResponseSize());
      }
      else {
        writeLine("Content-Size: " + getResponseText().length());
      }

      writeLine("");

      if (getRequest().isType(HTTPRequest.HEAD_REQUEST_TYPE)
              || getResponseCode() == 204) {
        return;
      }

      writeData();
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
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Send the actual response back to the client. <p>
   *
   * Where the actual data is sent back to the client. If a child handler
   * has non-generic text data to be sent, this is what should be modified.
   *
   * @see HTTPHandler#respond
   */
  public void writeData() throws IOException {
    writeLine(getResponseText());
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
  public String getResponseCodeMessage() {
    if (responses == null || responses.isEmpty()) {
      setupResponses();
    }

    if (responses.containsKey(getResponseCode())) {
      return getResponseCode() + " " + responses.get(getResponseCode());
    }

    return Integer.toString(getResponseCode());
  }

  /**
   * Sets up a list of response codes and text.
   */
  private void setupResponses() {
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


  /******************************
    Generic getters and setters
   ******************************/
  public void setRequest(HTTPRequest request) {
    this.request = request;
  }
  public HTTPRequest getRequest() {
    return request;
  }

  public void setResponseCode(int code) {
    responseCode = code;
  }
  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseSize(long size) {
    responseSize = size;
  }
  public long getResponseSize() {
    return responseSize;
  }

  public void setHandled(boolean handled) {
    this.handled = handled;
  }
  public boolean isHandled() {
    return handled;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }
  public String getResponseText() {
    return responseText;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }
  public String getResponseType() {
    return responseType;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }
  public Socket getSocket() {
    return socket;
  }

  public void setWriter(DataOutputStream writer) {
    this.writer = writer;
  }
  public DataOutputStream getWriter() {
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
    HTTPHandler.serverInfo = serverInfo;
  }
  public static String getServerInfo() {
    return serverInfo;
  }
}
package httpserver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Set;

/**
 * An HTTPHandler is what all handlers used by your server descend from. <p>
 * 
 * Extended classes have two options for determining their actions: they may
 * override the handle method (slightly harder), or use the addGet and addPost
 * methods in the constructor. See their descriptions for more information. <p>
 * 
 * If you just want to send a static message to the client, regardless of
 * request, you can use a MessageHandler, instead of creating a
 *
 * @see HTTPHandler#handle
 * @see HTTPHandler#addGet
 * @see HTTPHandler#addPost
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

  private HashMap<String, MethodWrapper> getMethods
  = new HashMap<String, MethodWrapper>();
  private HashMap<String, MethodWrapper> postMethods
  = new HashMap<String, MethodWrapper>();

  private HTTPRequest request;
  private int responseCode;
  private String responseType;
  private String responseText;
  private long responseSize;
  private boolean handled;

  /**
   * Create an HTTPHandler. <p>
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
   * @see HTTPResponse
   * @see HTTPRequest
   * @see HTTPHandler#setResponseCode
   * @see HTTPHandler#setResponseSize
   * @see HTTPHandler#setHandled
   * @see HTTPHandler#setResponseType
   */
  public HTTPHandler(HTTPRequest request) {
    setRequest(request);

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

    int mostCorrect = 0;

    if(method == null) {
      Set<String> keys = getMap().keySet();
      for (String key : keys) {
        MethodWrapper testMethod = getMap().get(key);
        int testCorrect = testMethod.howCorrect(path);

        if (testCorrect > mostCorrect) {
          method = testMethod;
          mostCorrect = testCorrect;
        }
      }
    }

    if (method == null) {
      if (getMap().containsKey("*")) {
        method = getMap().get("*");
      }
      else if (getMap().containsKey("/")) {
        method = getMap().get("/");
      }
    }

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
   * @return The HashMap for the correct request.
   */
  private HashMap<String, MethodWrapper> getMap() {
    if(getRequest().isType(HTTPRequest.GET_REQUEST_TYPE)) {
      return getMethods;
    }

    return postMethods;
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
   * @param path         Path to match
   * @param classMethod   Class and Method in class#method form.
   * @throws HTTPException When you do bad things.
   *
   * @see HTTPHandler#addPOST
   */
  public void addGET(String path, String methodName) throws HTTPException {
    addMethod(getMethods, path, methodName);
  }

  /**
   * Attach a method to a POST request at a path. <p>
   *
   * For a more detailed explanation, see addGET.
   *
   * @param path         Path to match
   * @param classMethod   Class and Method in class#method form.
   * @throws HTTPException When you do bad things.
   *
   * @see HTTPHandler#addGET
   */
  public void addPOST(String path, String methodName) throws HTTPException {
    addMethod(postMethods, path, methodName);
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
    MethodWrapper method
    = new MethodWrapper(path, methodName, this.getClass());
    map.put(path, method);
  }



  /**
   * Gets an absolute path from a relative path
   * @param path The relative path of a resource
   * @return The relative path's absolute path
   */
  public static String getResource(String path)
  {
    try
    {
      return URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(URLDecoder.decode(path, "UTF-8")).getPath(), "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      // This won't happen...
      e.printStackTrace();
    }
    return ClassLoader.getSystemClassLoader().getResource(path).getPath();
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
}
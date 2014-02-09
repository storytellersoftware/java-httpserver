package httpserver;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An HTTPHandler is what all handlers used by your server descend from. <p>
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
 * @see MessageHandler
 */
public abstract class HTTPHandler {
  private final HashMap<String, MethodWrapper> getMethods
          = new HashMap<String, MethodWrapper>();
  private final HashMap<String, MethodWrapper> postMethods
          = new HashMap<String, MethodWrapper>();
  private final HashMap<String, MethodWrapper> deleteMethods
          = new HashMap<String, MethodWrapper>();

  private Socket socket;
  private DataOutputStream writer;
  

  /**
   * Create an HTTPHandler. <p>
   * 
   * When writing your own HTTPHandler, this is where you should add the
   * handler's internal routing, as well performing any setup tasks. Handlers
   * are multi-use, which means that only one of any kind of handler should be
   * created in an application (unless you have custom needs). 
   *
   * @throws HTTPException  The exception typically comes from trying to add
   *                        a new method. In a standard configuration this will
   *                        keep the server from starting.
   */
  public HTTPHandler() throws HTTPException { }


  /**
   * Where the Handler handles the information given from the request and
   * based off of the paths specified in the Handler. <p>
   *
   * This can be overridden for more fine-grained handling. As is, it uses
   * the data behind the addGET, addPOST, and addDELETE methods for determining 
   * the correct action to take. <p>
   *
   * If there is not exact match, the `*` path is used. If you don't have a `*`
   * catchall route, a 501 (Not implemented) is sent to the client.
   *
   * @param request     The incoming HTTPRequest.
   * @param response    The outgoing HTTPResponse, waiting to be filled by an
   *                    HTTPHandler.
   *
   * @see HTTPHandler#addGET
   * @see HTTPHandler#addPOST
   * @see HTTPHandler#addDELETE
   * @see HTTPResponse#NOT_A_METHOD_ERROR
   */
  public void handle(HTTPRequest request, HTTPResponse response) {
    try {
      Map<String, MethodWrapper> map = getMap(request);
      String path = request.getPath().substring(1);
      MethodWrapper method = map.get(path);
  
      /*  If the above MethodWrapper is null (occurs when the requested path
          is dynamic), find the best fit method based on MethodWrapper's scoring
          technique (see MethodWrapper#howCorrect for more information).
       */
      if (method == null) {
        int bestFit = 0;
        Set<String> keys = map.keySet();
        for (String key : keys) {
          MethodWrapper testMethod = map.get(key);
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
        if (map.containsKey("*")) {
          method = map.get("*");
        }
      }
  
      /*  If, following the whole ordeal, no acceptable method is found, send the
          client a 501, Not a Method error.
       */
      if (method == null) {
        response.message(501, HTTPResponse.NOT_A_METHOD_ERROR);
        return;
      }
  
      System.out.println("Method Invoked: " + method);
      method.invoke(this, response, request, path);
    }
    catch (HTTPException e) {
      // Whilst attempting to find a method, or invoking a method, an error
      // occured, tell the client.
      response.error(500, HTTPResponse.EXCEPTION_ERROR, e);
    }
  }


  /**
   * Return the correct request methods map based on an HTTPRequest
   *
   * @param req   The incoming HTTPRequest. Used to determine the correct map.
   * @return  The HashMap for the correct request. Defaults to GET if
   *          the method isn't known.
   */
  private HashMap<String, MethodWrapper> getMap(HTTPRequest req) {
    if(req.isType(HTTPRequest.POST_REQUEST_TYPE)) {
      return postMethods;
    }
    if(req.isType(HTTPRequest.DELETE_REQUEST_TYPE)) {
      return deleteMethods;
    }

    return getMethods;
  }

  /**
   * Attach a method to a GET request at a path. <p>
   *
   * Methods are passed in as a String, and must be a member of the current
   * handler.<p>
   *
   * Path's should come in "/path/to/action" form. If the method requires
   * any parameters that aren't an HTTPResponse, HTTPRequest, or Map, 
   * they should be included in the path, in the order they're
   * listed in the method header, in "{ClassName}" form. Example:
   * <code>/hello/{String}/{String}</code> is a good path. <p>
   *
   * Methods being passed in must accept an HTTPResponse as their first
   * parameter. Methods may optionally accept an HTTPRequest and a 
   * Map&lt;String, String&gt; in that order (they may accept a Map but not an
   * HTTPRequest, but if they accept both the HTTPRequest must come first).
   *
   * Parameters following the above must be included in the java.lang library
   * and have a constructor that takes in a String. 
   * Any other type of parameter will cause an exception to occur. <p>
   *
   * Additionally, primitives are not permited, because they're not classes in
   * the java.lang library. The three most common parameter types are String,
   * Integer, and Double.
   *
   * @param path        Path to match
   * @param methodName  Method belonging to the current class, in String form.
   * @throws HTTPException When you do bad things.
   *
   * @see HTTPHandler#addPOST
   * @see HTTPHandler#addDELETE
   * @see HTTPResponse
   * @see HTTPRequest
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
   * Methods are passed in using "methodName", meaning they must be a member of
   * the current handler.
   *
   * @param map             The map to add this junks to.
   * @param path            Path to match
   * @param classMethod     Class and Method in class#method form.
   *
   * @throws HTTPException  When you do bad things.
   */
  private void addMethod(HashMap<String, MethodWrapper> map, String path,
          String methodName) throws HTTPException {

	  if (path.startsWith("/"))
		  path = path.substring(1);
	
    MethodWrapper method = new MethodWrapper(path, methodName, getClass());
    map.put(path, method);
  }
  
  
 
  /******************************
    Generic getters and setters
   ******************************/
  
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
}
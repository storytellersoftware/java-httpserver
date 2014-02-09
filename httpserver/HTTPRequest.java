package httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTPRequest takes an incoming connection and parses out all of the
 * relevant data, supposing the connection follows HTTP protocol.
 *
 * At present, HTTPRequest only knows how to handle HTTP 1.1 requests, and
 * doesn't handle persistent connections. Technically, it could handle an
 * HTTP 1.0 request, because 1.0 doesn't have persistent connections.
 *
 * @see   <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">
 *        HTTP 1.1 Spec</a>
 * @see HTTPHandler
 */
public class HTTPRequest implements Runnable {
  /** HTTP GET request type */
  public static final String GET_REQUEST_TYPE = "GET";

  /** HTTP POST request type */
  public static final String POST_REQUEST_TYPE = "POST";

  /** HTTP HEAD request type */
  public static final String HEAD_REQUEST_TYPE = "HEAD";

  /** HTTP DELETE request type */
  public static final String DELETE_REQUEST_TYPE = "DELETE";

  /** HTTP PUT request type */
  public static final String PUT_REQUEST_TYPE = "PUT";


  // used to determine what one does with the request
  private static HTTPRouter handlerFactory;

  // connection with client
  private Socket connection;

  // the handler used to determine what the server actually does
  // with this request
  private HTTPHandler handler;

  // the full text of the incoming request, including headers
  // and sent over data
  private String httpRequest;

  // the request line, or first line of entire request
  private String requestLine;

  // the type of request, as in GET, POST, ...
  private String requestType;

  // the protocol the client is using
  private String requestProtocol;

  // All headers, because they're all key/value pairs
  private Map<String, String> headers;

  // The requested path, split by '/'
  private List<String> splitPath;

  // The path relative to the handler's path
  private String path;

  // the full path
  private String fullPath;

  // the GET data
  private Map<String, String> getData;

  // the POST data
  private Map<String, String> postData;


  /**
   * Used to parse out an HTTP request provided a Socket and figure out the
   * handler to be used.
   *
   * @param connection The socket between the server and client
   * @throws IOException      When it gets thrown by
   *                          {@link HTTPRequest#parseRequest}.
   * @throws SocketException  When it gets thrown by
   *                          {@link HTTPRequest#parseRequest}.
   * @throws HTTPException    When something that doesn't follow HTTP spec
   *                          occurs.
   *
   * @see HTTPRequest#parseRequest
   */
  public HTTPRequest(Socket connection) throws IOException, SocketException,
          HTTPException {
    connection.setKeepAlive(true);
    setConnection(connection);

    setHeaders(new HashMap<String, String>());
    setSplitPath(new ArrayList<String>());
    setGetData(new HashMap<String, String>());
    setPostData(new HashMap<String, String>());
  }

  @Override
  public void run() {
    if (getConnection().isClosed())
      System.out.println("Socket is closed...");
    
    try {
      parseRequest();
      HTTPResponse resp = new HTTPResponse(this);
      determineHandler().handle(this, resp);
      resp.respond();
      
    } catch (IOException | HTTPException e) {
      e.printStackTrace();
    }
    
  }
  

  /**
   * Kicks off the request's parsing. Called inside constructor.
   *
   * @throws IOException      When an InputStream can't be retreived from the
   *                          socket.
   * @throws SocketException  When the client breaks early. This is a browser
   *                          issue, and not a server issue, but it gets thrown
   *                          upstream because it can't be dealt with until it
   *                          gets to the HTTPServer.
   * @throws HTTPException    When headers aren't in key/value pairs separated
   *                          by ": ".
   *
   * @see HTTPServer
   */
  private void parseRequest() throws IOException, SocketException,
  HTTPException {
    // Used to read in from the socket
    BufferedReader input = new BufferedReader(
            new InputStreamReader(getConnection().getInputStream()));

    StringBuilder requestBuilder = new StringBuilder();

    /*  The HTTP spec (Section 4.1) says that a blank first line should be
        ignored, and that the next line SHOULD have the request line. To be
        extra sure, all initial blank lines are discarded.
    */
    String firstLine = input.readLine();
    if (firstLine == null)
    	throw new HTTPException("Input is returning nulls...", new NullPointerException());
    
    while (firstLine.isEmpty()) {
      firstLine = input.readLine();
    }

    // start with the first non-empty line.
    setRequestLine(firstLine);
    requestBuilder.append(getRequestLine());
    requestBuilder.append("\n");

    /*  Every line after the first, but before an empty line is a header,
        which is a key/value pair.

        The key is before the ": ", the value, after
     */
    for (String line = input.readLine(); line != null && !line.isEmpty();
            line = input.readLine()) {
      requestBuilder.append(line);
      requestBuilder.append("\n");

      String[] items = line.split(": ");

      if (items.length == 1) {
        throw new HTTPException("No key value pair in \n\t" + line);
      }

      String value = items[1];
      for (int i = 2; i < items.length; i++) {
        value += ": " + items[i];
      }

      getHeaders().put(items[0], value);
    }
    

    /*  If the client sent over a POST request, there's *probably* still data
        in the stream. This reads in only the number of chars specified in the
        "Content-Length" header.
     */
    if (getRequestType().equals(POST_REQUEST_TYPE) &&
            getHeaders().containsKey("Content-Length")) {
      int contentLength = Integer.parseInt(getHeaders().get("Content-Length"));
      StringBuilder b = new StringBuilder();

      for (int i = 0; i < contentLength; i++) {
        b.append((char)input.read());
      }
      
      requestBuilder.append(b.toString());

      String[] data = b.toString().split("&");
      getPostData().putAll(parseInputData(data));
    }
    
    setHTTPRequest(requestBuilder.toString());
  }


  /**
   * Turns an array of "key=value" strings into a map. <p>
   * 
   * Any item in the array missing an "=" is ignored, and not added to the
   * returned map. If an empty value is wanted, an "=" is required at the end
   * of the key.
   *
   * @param data  List of strings in "key=value" form, you know, like HTTP GET
   *              or POST lines?
   * @return  Map of key value pairs
   */
  private Map<String, String> parseInputData(String[] data) {
    Map<String, String> out = new HashMap<String, String>();
    for (String item : data) {
      if (item.indexOf("=") == -1) {
        continue;
      }

      String value = item.substring(item.indexOf('=') + 1);

      /*  Attempt to URL decode the value, because it *might* be user input.
          If it can't be decoded, it doesn't matter, the original, undecoded
          value is still used.
       */
      try {
        value = URLDecoder.decode(value, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {}

      out.put(item.substring(0, item.indexOf('=')), value);
    }

    return out;
  }

  /**
   * Figure out what kind of HTTPHandler you want, based on the path. <p>
   *
   * This uses the statically set {@link HTTPRouter} to determine the
   * correct HTTPHandler to be used for the current request. If there isn't
   * a statically set HTTPHandlerFactory, a 500 error is sent back to the
   * client.
   *
   * @return a new instance of some form of HTTPHandler.
   *
   * @see HTTPRouter
   * @see HTTPRouter#determineHandler
   * @see HTTPHandler
   */
  public HTTPHandler determineHandler() throws HTTPException {
    if (handlerFactory == null) {
      return new DeathHandler();
    }
    
    String path = getSplitPath().isEmpty() ? "" : getSplitPath().get(0);
    return handlerFactory.route(path, this);
  }

  /**
   * Return if the request type is the passed in type.
   * @param requestTypeCheck The type to check.
   * @return whether the request type equals the passed in String.
   */
  public boolean isType(String requestTypeCheck) {
    return getRequestType().equalsIgnoreCase(requestTypeCheck);
  }

  /**
   * Sets the requestLine, and all derived items. <p>
   * 
   * Based off of the passed in line, the request type, request path, and
   * request protocol can be set.
   *
   * @param line  The first line in an HTTP request. Should be in
   *              {@code [type] [full path] [protocol]}.
   * @throws HTTPException  When the first line does not contain two spaces,
   *                        signifying that the passed in line is not in
   *                        HTTP 1.1. When the type is not an expected type
   *                        (currently GET, POST, and HEAD).
   *
   * @see HTTPRequest#setRequestType
   * @see HTTPRequest#setFullPath
   * @see HTTPRequest#setRequestProtocol
   */
  public void setRequestLine(String line) throws HTTPException {
    this.requestLine = line;

    /*  Split apart the request line by spaces, as per the protocol.
        The request line should be:
          [request type] [path] [protocol]
     */
    String[] splitty = requestLine.split(" ");
    if (splitty.length < 3) {
      throw new HTTPException("Incomplete request line");
    }


    // Set the request type
    if (splitty[0].equalsIgnoreCase(GET_REQUEST_TYPE)) {
      setRequestType(GET_REQUEST_TYPE);
    }
    else if (splitty[0].equalsIgnoreCase(POST_REQUEST_TYPE)) {
      setRequestType(POST_REQUEST_TYPE);
    }
    else if (splitty[0].equalsIgnoreCase(HEAD_REQUEST_TYPE)) {
      setRequestType(HEAD_REQUEST_TYPE);
    }
    else if (splitty[0].equalsIgnoreCase(DELETE_REQUEST_TYPE)) {
      setRequestType(DELETE_REQUEST_TYPE);
    }
    else {
      throw new HTTPException("Unexpected request type: " + splitty[0]);
    }

    // set the path
    setFullPath(splitty[1]);

    // set the protocol type
    setRequestProtocol(splitty[2]);
  }
  /**
   * Return the request line.
   * @return  the request line.
   */
  public String getRequestLine() {
    return requestLine;
  }


  /**
   * Set the full path, and path list. <p>
   *
   * Because the path list is derived from the full path, it's set at the same
   * time.
   *
   * @param inPath  The full requested path (in `/path/to/request` form)
   *
   * @see HTTPRequest#setPath
   * @see HTTPRequest#setSplitPath
   */
  public void setFullPath(String inPath) {
    this.fullPath = inPath;
    setPath(inPath);
    setSplitPath(inPath);
  }
  /**
   * Gets the full path of the request.
   * @return The full path.
   */
  public String getFullPath() {
    return fullPath;
  }

  public void setPath(String path) {
    this.path = path;
  }
  /**
   * Gets the path relative to the handler's path.
   * @return Everything in the path after the handler's path.
   */
  public String getPath() {
    return path;
  }


  /**
   * Given a full path, set the splitPath to the path, split by `/`. <p>
   *
   * If there's a query string attached to the path, it gets removed from the
   * splitPath, and the request's associated GET data is parsed from the query
   * string.
   *
   * @see HTTPRequest#getGetData
   */
  public void setSplitPath(String fullPath) {
    /*  Split apart the path for future reference by the handlers
        The split path should be used by handlers to figure out what
        action should be taken. It's also used to parse out GET request
        data.

        The first character *should* always be a `/`, and that could cause
        an error with splitting (as in, the first split could be an empty
        string, which we don't want).
     */
    for (String segment : fullPath.substring(1).split("/")) {
      if (segment.isEmpty()) {
        continue;
      }

      getSplitPath().add(segment);
    }

    if (getSplitPath().isEmpty()) {
      return;
    }

    /*  Parse out any GET data in the request URL.
        This could occur on any request.
     */
    if (getSplitPath().get(getSplitPath().size() - 1).indexOf('?') != -1) {
      String lastItem = getSplitPath().get(getSplitPath().size() - 1);
      // remove the ? onward from the last item in the path, because that's not
      // part of the requested URL
      getSplitPath().set(getSplitPath().size() - 1, lastItem.substring(0,
              lastItem.indexOf('?')));

      // split apart the request query into an array of "key=value" strings.
      String[] data = lastItem.substring(lastItem.indexOf('?') + 1).split("&");

      // Set the GET data to the GET data...
      getGetData().putAll(parseInputData(data));
    }
  }
  public void setSplitPath(List<String> path) {
    this.splitPath = path;
  }
  /**
   * Gets the path relative to the handler's path split by '/'
   * @return A List of Strings
   */
  public List<String> getSplitPath() {
    return splitPath;
  }



  public void setConnection(Socket connection) {
    this.connection = connection;
  }
  public Socket getConnection() {
    return connection;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }
  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setGetData(Map<String, String> data) {
    this.getData = data;
  }
  public Map<String, String> getGetData() {
    return getData;
  }

  public void setPostData(Map<String, String> data) {
    this.postData = data;
  }
  public Map<String, String> getPostData() {
    return postData;
  }

  public void setHTTPRequest(String httpRequest) {
    this.httpRequest = httpRequest;
  }
  public String getHTTPRequest() {
    return httpRequest;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }
  public String getRequestType() {
    return requestType;
  }

  public void setRequestProtocol(String requestProtocol) {
    this.requestProtocol = requestProtocol;
  }
  public String getRequestProtocol() {
    return requestProtocol;
  }

  public void setHandler(HTTPHandler handler) {
    this.handler = handler;
  }
  public HTTPHandler getHandler() {
    return handler;
  }

  public static void setHandlerFactory(HTTPRouter handlerFactory) {
    HTTPRequest.handlerFactory = handlerFactory;
  }
  public static HTTPRouter getHTTPHandlerFactory() {
    return handlerFactory;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("HTTPRequest from ");
    builder.append(getConnection().getLocalAddress().getHostAddress());
    builder.append("\n\t");
    builder.append("Request Line: ");
    builder.append(getRequestLine());
    builder.append("\n\t\t");
    builder.append("Request Type ");
    builder.append(getRequestType());
    builder.append("\n\t\t");
    builder.append("Request Path ");
    builder.append(getFullPath());

    return builder.toString();
  }
}
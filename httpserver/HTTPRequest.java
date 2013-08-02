package httpserver;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;

/**
 * An HTTPRequest takes an incoming connection and parses out all of the
 * relevant data, supposing the conneciton follows HTTP protocol. 
 */
public class HTTPRequest {
  /** HTTP GET request type */
  public static final String GET_REQUEST_TYPE = "GET";

  /** HTTP POST request type */
  public static final String POST_REQUEST_TYPE = "POST";

  /** HTTP HEAD request type */
  public static final String HEAD_REQUEST_TYPE = "HEAD";


  // used to determine what one does with the request
  private static Map<String, Class<? extends HTTPHandler>> handlers 
          = new HashMap<String, Class<? extends HTTPHandler>>();

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
  private List<String> path;

  // the full path
  private String fullPath;

  // the GET data
  private Map<String, String> getData;

  // the POST data
  private Map<String, String> postData;


  /**
   * Used to parse out an HTTP request provided a Socket and figure out the
   * handler to be used.
   * @param connection The socket between the server and client
   * @throws IOException 
   * @throws SocketException
   * @throws HTTPException when something that doesn't follow HTTP spec occurs
   */
  public HTTPRequest(Socket connection) throws IOException, SocketException, 
          HTTPException {
    if (handlers.isEmpty()) {
      handlers.put("*", DeathHandler.class);
    }

    setConnection(connection);

    setHeaders(new HashMap<String, String>());
    setPath(new ArrayList<String>());
    setGetData(new HashMap<String, String>());
    setPostData(new HashMap<String, String>());

    parseRequest();
    setHandler(determineHandler());
  }


  /**
   * Kicks off the request's parsing. Called inside constructor.
   * @throws IOException
   * @throws SocketException
   * @throws HTTPException
   */
  private void parseRequest() throws IOException, SocketException, 
          HTTPException {
    // Used to read in from the socket
    BufferedReader input = new BufferedReader(
            new InputStreamReader(getConnection().getInputStream()));

    // The first line of a request *should* be the request line
    setRequestLine(input.readLine());

    /*  Every line after the first, but before an empty line is a header,
        which is a key/value pair.

        The key is before the ": ", the value, after
    */
    for (String line = input.readLine(); 
            line != null && !line.isEmpty(); 
            line = input.readLine()) {
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

      String[] data = b.toString().split("&");
      getPostData().putAll(parseInputData(data));
    }
  }


  /**
   * Turns an array of "key=value" strings into a map.
   * @param   data List of strings in "key=value" form, you know, like HTTP GET 
   *          or POST lines?
   * @return  Map of key value pairs
   */
  private Map<String, String> parseInputData(String[] data) {
    Map<String, String> out = new HashMap<String, String>();
    for (String item : data) {
      String value = item.substring(item.indexOf('=') + 1);

      try {
        value = URLEncoder.encode(value, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {}

      out.put(item.substring(0, item.indexOf('=')), value);
    }

    return out;
  }


  /**
   * Add a new handler to the list of available handlers.
   *
   * A request's handler type is determined based off of the first
   * segment of a path. For example if 
   * <code>addHandler("add", AdditionHandler.class)</code> is called,
   * when someone makes a request to 
   * <code>/add/[any number of other path segments]</code>, a new
   * AdditionHandler is used to determine the response's content.
   *
   * @param path  When the first segment of the path matches this, 
   *              the passed in Handler's class is called.
   * @param handlerClass  The class of the HTTPHandler to be called
   *                      when the above path is matched
   */
  public static void addHandler(String path, Class<? extends HTTPHandler> handlerClass) {

    handlers.put(path, handlerClass);
  }

  /**
   * Figure out what kind of HTTPHandler you want, based on the path.
   *
   * This returns a new object of that class. Just learning that Java has 
   * this capability really impressed me, I wouldn't have thought Java could 
   * do this.
   *
   * @return a new instance of some form of HTTPHandler.
   */
  public HTTPHandler determineHandler() {
    Class<? extends HTTPHandler> hClass;
    
    // check if there's a handler for the specified segment.
    if (handlers.containsKey(getPath().get(0))) {
      hClass = handlers.get(getPath().get(0));
      getPath().remove(0);
    }
    // if there isn't, use our default handler
    else {
      hClass = handlers.get("*");
    }

    try {
      // attempt to make a new constructor of the selected class from
      // above. And attempt to return a new object of that class, using
      // the constructor.
      Constructor<? extends HTTPHandler> hConstructor 
              = hClass.getConstructor(HTTPRequest.class);

      return hConstructor.newInstance(this);
    } 
    catch (NoSuchMethodException | SecurityException | InstantiationException 
            | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
      e.printStackTrace();
    }
  
    // if we can't do that, send over a DEATH HANDLER!
    return new DeathHandler(this);
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

  /**
   * Sets the requestLine, and all derived items.
   * 
   * Based off of the passed in line, the request type, request path, and request
   * protocol can be set.
   * @param line The first line in an HTTP request
   * @throws HTTPException
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
    else {
      throw new HTTPException("Unexpected request type: " + splitty[0]);
    }

    // set the path
    setFullPath(splitty[1]);

    // set the protocol type
    setRequestProtocol(splitty[2]);
  }
  public String getRequestLine() {
    return requestLine;
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

  /**
   * Set the full path, and path list.
   * Because the path list is derived from the full path, it's set at the same time.
   * @param inPath The full requested path (in `/path/to/request` form)
   */
  public void setFullPath(String inPath) {
    this.fullPath = inPath;

    /*  Split apart the path for future reference by the handlers
        The split path should be used by handlers to figure out what
        action should be taken. It's also used to parse out GET request
        data.

        The first character *should* always be a `/`, and that could cause
        an error with splitting (as in, the first split could be an empyt
        string, which we don't want).
    */
    for (String segment : fullPath.substring(1).split("/")) {
      getPath().add(segment);
    }

    /*  Parse out any GET data in the request URL.
        This could occur on any request.
    */
    if (getPath().get(getPath().size() - 1).indexOf('?') != -1) {
      String lastItem = getPath().get(getPath().size() - 1);
      // remove the ? onward from the last item in the path, because that's not
      // part of the requested URL
      getPath().set(getPath().size() - 1, lastItem.substring(0, lastItem.indexOf('?')));

      // split apart the request query into an array of "key=value" strings.
      String[] data = lastItem.substring(lastItem.indexOf('?') + 1).split("&");

      // Set the GET data to the GET data...
      getGetData().putAll(parseInputData(data));
    }
  }
  public String getFullPath() {
    return fullPath;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }
  public List<String> getPath() {
    return path;
  }

  public void setHandler(HTTPHandler handler) {
    this.handler = handler;
  }
  public HTTPHandler getHandler() {
    return handler;
  }

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
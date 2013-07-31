package httpserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPRequest {
  public static final String GET_REQUEST_TYPE = "GET";
  public static final String POST_REQUEST_TYPE = "POST";
  public static final String HEAD_REQUEST_TYPE = "HEAD";

  // connection with client
  private Socket connection;

  // the handler used to determine what the server actually does
  // with this request
  //private HTTPHandler handler;

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
   * An HTTPRequest takes a supposed HTTP request (supposed because
   * a user <em>could</em> use something other than a web browser to
   * make a call to the server) and dismantles it into it's base components.
   * @param connection The socket between the server and client
   * @throws IOException
   * @throws SocketException
   * @throws HTTPException
   */
  public HTTPRequest(Socket connection) throws IOException, SocketException, HTTPException {
    setConnection(connection);

    setHeaders(new HashMap<String, String>());
    setPath(new ArrayList<String>());
    setGetData(new HashMap<String, String>());
    setPostData(new HashMap<String, String>());

    parseRequest();
  }


  /**
   * Kicks off the request's parsing. Called inside constructor.
   * @throws IOException
   * @throws SocketException
   * @throws HTTPException
   */
  private void parseRequest() throws IOException, SocketException, HTTPException {
    // Used to read in from the socket
    BufferedReader input = new BufferedReader(new InputStreamReader(getConnection().getInputStream()));

    // The first line of a request *should* be the request line
    setRequestLine(input.readLine());
  }


  /**
   * Turns an array of "key=value" strings into a map.
   * @param data List of strings in "key=value" form, you know, like HTTP GET or POST lines?
   * @return
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
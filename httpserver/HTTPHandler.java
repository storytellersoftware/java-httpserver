package httpserver;

/**
 * An HTTPHandler is what all handlers used by your server descend from.
 * The only real requirement for a handler is that it has a <code>handle</code>
 * method, where it sets the values that are going to be used by the
 * HTTPResponse.
 */
public abstract class HTTPHandler {
  public static final String EXCEPTION_ERROR = "an exception occured while processing your request";
  public static final String NOT_A_METHOD_ERROR = "No known method";
  public static final String MALFORMED_INPUT_ERROR = "Malformed Input";

  public static final String STATUS_GOOD = "All systems are go";

  private HTTPRequest request;
  private int responseCode;
  private String responseType;
  private String responseText;
  private int responseSize;
  private boolean handled;

  /**
   * Create an HTTPHandler.
   * @param request HTTPRequest with the browser's request.
   */
  public HTTPHandler(HTTPRequest request) {
    setRequest(request);

    // default to good things
    setResponseCode(200);
    setResponseSize(-1);
    setHandled(false);
    setResponseType("text/plain");
  }

  /**
   * Where subclasses perform their specific actions.
   */
  public abstract void handle();

  /**
   * Send a simple string message with an HTTP response code back to
   * the client. Can be used for sending all data back.
   * @param code An HTTP response code.
   * @param message The content of the server's response to the browser
   */
  public void message(int code, String message) {
    setResponseCode(code);
    setResponseText(message);
    setHandled(true);
  }

  /**
   * Tell the browser there is no response data.
   * This is done by sending over a 204 code, which means there isn't
   * any data in the stream, but the server correctly processed the request
   */
  public void noContent() {
    setResponseCode(204);
    setResponseText("");
    setHandled(true);
  }

  /**
   * Same as message(), but prints out an exception.
   *
   * @see httpserver.HTTPHandler#message
   * @param code HTTP status code
   * @param message the content being sent back to the browser
   * @param t A throwable object, to be printed to the screen
   */
  public void error(int code, String message, Throwable t) {
    t.printStackTrace();
    message(code, message);
  }
  
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

  public void setResponseSize(int size) {
    responseSize = size;
  }
  public int getResponseSize() {
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
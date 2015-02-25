package httpserver;

/**
 * A MessageHandler is a simple handler that sends a simple text message to
 * the client.
 *
 * A MessageHandler solves the problem of sending a simple message back to the
 * client regardless of the request, without requiring developers to create a
 * new HttpHandler.
 */
public class MessageHandler extends HttpHandler {

  private String body;
  private int code;
  
  /**
   * Create a message handler
   *
   * All it does is call <code>message(code, message)</code>.
   *
   * @param request   The associated HttpRequest. Required by all Handlers.
   * @param code      An HTTP status code to be used with the attached message.
   * @param message   A simple text message to be sent to the client.
   *
   * @see HttpHandler#message
   * @see HttpRequest
   */
  public MessageHandler(int code, String message) throws HttpException {
    body = message;
    this.code = code;
  }

  /**
   * Create a message handler, with the HTTP status set to 200
   *
   * Calls <code>message(200, message)</code>.
   *
   * @param message   A simple text message to be sent to the client with the
   *                  HTTP status code of 200.
   *
   * @see HttpHandler#message
   * @see HttpRequest
   */
  public MessageHandler(String message) throws HttpException {
    this(200, message);
  }
  
  public void handle(HttpRequest req, HttpResponse resp) {
    resp.message(code, body);
  }

}

package httpserver;

/**
 * A MessageHandler is a simple handler that sends a simple text message to
 * the client.
 *
 * A MessageHandler solves the problem of sending a simple message back to the
 * client regardless of the request, without requiring developers to create a
 * new HTTPHandler.
 */
public class MessageHandler extends HTTPHandler {

  /**
   * Create a message handler
   *
   * All it does is call <code>message(code, message)</code>.
   *
   * @param request   The associated HTTPRequest. Required by all Handlers.
   * @param code      An HTTP status code to be used with the attached message.
   * @param message   A simple text message to be sent to the client.
   *
   * @see HTTPHandler#message
   * @see HTTPRequest
   */
  public MessageHandler(HTTPRequest request, int code, String message) {
    setRequest(request);
    message(code, message);
  }

  /**
   * Create a message handler, with the HTTP status set to 200
   *
   * Calls <code>message(200, message)</code>.
   *
   * @param message   A simple text message to be sent to the client with the
   *                  HTTP status code of 200.
   *
   * @see HTTPHandler#message
   * @see HTTPRequest
   */
  public MessageHandler(HTTPRequest request, String message)
          throws HTTPException {
    this(request, 200, message);
  }

}
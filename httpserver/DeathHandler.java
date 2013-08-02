package httpserver;

/**
 * A DeathHandler should only be called if something bad occurs.
 *
 * The DeathHandler is used on the backend to send a 500 message to the browser
 * if all of the other handlers fail to do things. Which they shouldn't.
 *
 * It's also set as the initial wildcard handler, meaning if there aren't any
 * other handlers available, it'll be used.
 *
 * TODO:  at some point, maybe create a list of potential error phrases
 *        in a list, and have handle randomly select a message to be sent back
 *        to the browser. It could be fun!
 */
class DeathHandler extends HTTPHandler {

  /**
   * Creates a new DeathHandler...
   */
  public DeathHandler(HTTPRequest request) {
    super(request);
  }

  /**
   * Always return a 500 error.
   *
   * Regardless of what you *think* we should do, we're just going to send a
   * 500 error to the browser, with the message "Well, that went well...".
   */
  public void handle() {
    message(500, "Well, that went well...");
  }
}
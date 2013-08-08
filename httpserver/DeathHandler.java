package httpserver;

import java.util.Random;
import java.util.ArrayList;

/**
 * A DeathHandler should only be called if something bad occurs.
 *
 * The DeathHandler is used on the backend to send a 500 message to the
 * browser if all of the other handlers fail to do things. Which they
 * shouldn't.
 *
 * It's also set as the initial wildcard handler, meaning if there aren't any
 * other handlers available, it'll be used.
 *
 * TODO:  at some point, maybe create a list of potential error phrases
 *        in a list, and have handle randomly select a message to be sent back
 *        to the browser. It could be fun!
 */
class DeathHandler extends HTTPHandler {

  public static ArrayList<String> errorMessages;

  /**
   * Creates a new DeathHandler...
   */
  public DeathHandler(HTTPRequest request) throws HTTPException {
    super(request);

    if (errorMessages == null || errorMessages.isEmpty()) {
      setupErrorMessages();
    }
  }

  /**
   * Always return a 500 error.
   *
   * Regardless of what you *think* we should do, we're just going to send a
   * 500 error to the browser, with a random, generic error message. Including
   * some from our good friend, Han Solo.
   */
  @Override
  public void handle() {
    String message = errorMessages.get(
            new Random().nextInt(errorMessages.size()));

    message(500, message);
  }


  /**
   * Setup error messages that could be sent to the client
   */
  private static void setupErrorMessages() {
    errorMessages = new ArrayList<String>();

    errorMessages.add("Well, that went well...");
    errorMessages.add("That's not a good sound.");
    errorMessages.add("Oh God, oh God, we're all gonna die.");
    errorMessages.add("What a crazy random happenstance!");
    errorMessages.add("Uh, everything's under control. Situation normal.");
    errorMessages.add("Uh, we had a slight weapons malfunction, but, uh... "
            + "everything's perfectly all right now. We're fine. We're all "
            + "fine here now, thank you. How are you?");
    errorMessages.add("Definitely feeling aggressive tendency, sir!");

  }
}
package demo;

import httpserver.*;


/**
 * A simple example of how requests work.<p>
 * 
 * It says hello/goodbye to the user depending on what is requested.
 *
 */
public class HelloHandler extends HTTPHandler {

  public HelloHandler(HTTPRequest request) throws HTTPException {
    super(request);

    try {
      addGET("/", "sayHello");
      addGET("/{String}/", "sayHello");
      addGET("/{String}/{String}", "sayHello");

      addDELETE("/goodbye", "sayGoodbye");
    } catch (HTTPException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the text: "Hellow World".
   */
  public void sayHello() {
    setResponseText("Hello World");
    setHandled(true);
  }

  /**
   * Returns the text: "Hello {firstName}".
   * @param firstName
   */
  public void sayHello(String firstName) {
    String response = "Hello " + firstName;
    setResponseText(response);
    setHandled(true);
  }

  /**
   * Returns the text: "Hello {firstName} {lastName}".
   * @param firstName
   * @param lastName
   */
  public void sayHello(String firstName, String lastName) {
    String response = "Hello " + firstName + " " + lastName;
    setResponseText(response);
    setHandled(true);
  }

  /**
   * Returns the text: "Goodbye World".
   */
  public void sayGoodbye() {
    setResponseText("Goodbye World");
    setHandled(true);
  }
}
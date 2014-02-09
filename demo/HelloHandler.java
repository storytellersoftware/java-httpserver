package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPResponse;


/**
 * A simple example of how requests work.<p>
 * 
 * It says hello/goodbye to the user depending on what is requested.
 *
 */
public class HelloHandler extends HTTPHandler {

  public HelloHandler() throws HTTPException {
    addGET("/", "sayHello");
    addGET("/{String name}/", "sayHello");
    addGET("/{String} first/{String} last", "sayHello");

    addDELETE("/goodbye", "sayGoodbye");
  }

  /**
   * Returns the text: "Hello World".
   */
  public void sayHello(HTTPResponse resp) {
    resp.setBody("Hello World");
  }

  /**
   * Returns the text: "Hello {firstName}".
   * @param firstName
   */
  public void sayHello(HTTPResponse resp, String firstName) {
    String response = "Hello " + firstName;
    resp.setBody(response);
  }

  /**
   * Returns the text: "Hello {firstName} {lastName}".
   * @param firstName
   * @param lastName
   */
  public void sayHello(HTTPResponse resp, String firstName, String lastName) {
    String response = "Hello " + firstName + " " + lastName;
    resp.setBody(response);
  }

  /**
   * Returns the text: "Goodbye World".
   */
  public void sayGoodbye(HTTPResponse resp) {
    resp.setBody("Goodbye World");
  }
}
package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;
import httpserver.HTTPResponse;

/**
 * A slightly complex example of how to setup requests.<p>
 * 
 * It allows a user to do some simple math.
 *
 */
public class MathHandler extends HTTPHandler {

  public MathHandler() throws HTTPException {
    addGET("*", "noMath");
    addGET("/add/{Double}/{Double}", "add");
    addGET("/subtract/{Double}/{Double}", "subtract");
    addGET("/multiply/{Double}/{Double}", "multiply");
    addGET("/divide/{Double}/{Double}", "divide");

    addGET("/add/{Integer}/{Integer}", "add");
    addGET("/subtract/{Integer}/{Integer}", "subtract");
    addGET("/multiply/{Integer}/{Integer}", "multiply");
    addGET("/divide/{Integer}/{Integer}", "divide");
  }

  // Double methods
  /**
   * Add two Doubles.
   * @param one
   * @param two
   */
  public void add(HTTPResponse resp, Double one, Double two) {
    resp.message(200, one + " + " + two + " = " + (one + two));
    resp.setHeader("Math-Type", "addition");
  }

  /**
   * Subtract two Doubles.
   * @param one
   * @param two
   */
  public void subtract(HTTPResponse resp, Double one, Double two) {
    resp.message(200, one + " - " + two + " = " + (one - two));
    resp.setHeader("Math-Type", "subtraction");
  }

  /**
   * Multiply two Doubles.
   * @param one
   * @param two
   */
  public void multiply(HTTPResponse resp, Double one, Double two) {
    resp.message(200, one + " * " + two + " = " + (one * two));
  }

  /**
   * Divide two Doubles.
   * @param one
   * @param two
   */
  public void divide(HTTPResponse resp, Double one, Double two) {
    resp.message(200, one + " / " + two + " = " + (one / two));
  }

  // Integer methods
  /**
   * Add two Integers.
   * @param one
   * @param two
   */
  public void add(HTTPResponse resp, Integer one, Integer two) {
    resp.message(200, one + " + " + two + " = " + (one + two));
  }

  /**
   * Subtract two Integers.
   * @param one
   * @param two
   */
  public void subtract(HTTPResponse resp, Integer one, Integer two) {
    resp.message(200, one + " - " + two + " = " + (one - two));
  }

  /**
   * Multiply two Integers.
   * @param one
   * @param two
   */
  public void multiply(HTTPResponse resp, Integer one, Integer two) {
    resp.message(200, one + " * " + two + " = " + (one * two));
  }

  /**
   * Divide two Integers.
   * @param one
   * @param two
   */
  public void divide(HTTPResponse resp, Integer one, Integer two) {
    resp.message(200, one + " / " + two + " = " + (one / two));
  }

  /**
   * If the user requests something that is not a route, this is used.
   */
  public void noMath(HTTPResponse resp, HTTPRequest req) {
    resp.message(418, "Not an operation");
  }
}
package demo;

import httpserver.*;
import java.net.*;

/**
 * A slightly complex example of how to setup requests.<p>
 * 
 * It allows a user to do some simple math.
 *
 */
public class MathHandler extends HTTPHandler {

  public MathHandler(Socket sock, HTTPRequest request) throws HTTPException {
    super(sock, request);

    try {
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
    catch (HTTPException e) {
      e.printStackTrace();
      message(500, EXCEPTION_ERROR);
    }
  }

  // Double methods
  /**
   * Add two Doubles.
   * @param one
   * @param two
   */
  public void add(Double one, Double two) {
    message(200, one + " + " + two + " = " + (one + two));
  }

  /**
   * Subtract two Doubles.
   * @param one
   * @param two
   */
  public void subtract(Double one, Double two) {
    message(200, one + " - " + two + " = " + (one - two));
  }

  /**
   * Multiply two Doubles.
   * @param one
   * @param two
   */
  public void multiply(Double one, Double two) {
    message(200, one + " * " + two + " = " + (one * two));
  }

  /**
   * Divide two Doubles.
   * @param one
   * @param two
   */
  public void divide(Double one, Double two) {
    message(200, one + " / " + two + " = " + (one / two));
  }

  // Integer methods
  /**
   * Add two Integers.
   * @param one
   * @param two
   */
  public void add(Integer one, Integer two) {
    message(200, one + " + " + two + " = " + (one + two));
  }

  /**
   * Subtract two Integers.
   * @param one
   * @param two
   */
  public void subtract(Integer one, Integer two) {
    message(200, one + " - " + two + " = " + (one - two));
  }

  /**
   * Multiply two Integers.
   * @param one
   * @param two
   */
  public void multiply(Integer one, Integer two) {
    message(200, one + " * " + two + " = " + (one * two));
  }

  /**
   * Divide two Integers.
   * @param one
   * @param two
   */
  public void divide(Integer one, Integer two) {
    message(200, one + " / " + two + " = " + (one / two));
  }

  /**
   * If the user requests something that is not a route, this is used.
   */
  public void noMath() {
    message(418, "Not an operation");
  }
}
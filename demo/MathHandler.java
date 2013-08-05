package demo;

import httpserver.*;

public class MathHandler extends HTTPHandler {

  public MathHandler(HTTPRequest request) throws HTTPException {
    super(request);

    try {
      addGET("*", "noMath");
      addGET("/add/{Double}/{Double}", "add");
      addGET("/subtract/{Double}/{Double}", "subtract");
      addGET("/multiply/{Double}/{Double}", "multiply");
      addGET("/divide/{Double}/{Double}", "divide");
    }
    catch (HTTPException e) {
      e.printStackTrace();
      message(500, EXCEPTION_ERROR);
    }
  }

  public void add(Double one, Double two) {
    message(200, one + " + " + two + " = " + (one + two));
  }

  public void subtract(Double one, Double two) {
    message(200, one + " - " + two + " = " + (one - two));
  }

  public void multiply(Double one, Double two) {
    message(200, one + " * " + two + " = " + (one * two));
  }

  public void divide(Double one, Double two) {
    message(200, one + " / " + two + " = " + (one / two));
  }

  public void noMath() {
    message(418, "Not an operation");
  }
}
package demo;

import httpserver.*;

public class MathHandler extends HTTPHandler {

  public MathHandler(HTTPRequest request) throws HTTPException {
    super(request);

    try {
      addGET("*", "noMath");
      addGET("/math/add/{Double}/{Double}", "add");
      addGET("/math/subtract/{Double}/{Double}", "subtract");
      addGET("/math/multiply/{Double}/{Double}", "multiply");
      addGET("/math/divide/{Double}/{Double}", "divide");

      addGET("/math/add/{Integer}/{Integer}", "add");
      addGET("/math/subtract/{Integer}/{Integer}", "subtract");
      addGET("/math/multiply/{Integer}/{Integer}", "multiply");
      addGET("/math/divide/{Integer}/{Integer}", "divide");
    }
    catch (HTTPException e) {
      e.printStackTrace();
      message(500, EXCEPTION_ERROR);
    }
  }

  // Double methods
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

  // Integer methods
  public void add(Integer one, Integer two) {
    message(200, one + " + " + two + " = " + (one + two));
  }

  public void subtract(Integer one, Integer two) {
    message(200, one + " - " + two + " = " + (one - two));
  }

  public void multiply(Integer one, Integer two) {
    message(200, one + " * " + two + " = " + (one * two));
  }

  public void divide(Integer one, Integer two) {
    message(200, one + " / " + two + " = " + (one / two));
  }

  public void noMath() {
    message(418, "Not an operation");
  }
}
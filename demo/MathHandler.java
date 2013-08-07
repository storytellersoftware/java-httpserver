package demo;

import httpserver.*;
import java.io.*;
import java.net.*;

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
package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;

public class MathArrayHandler extends HTTPHandler {

  public MathArrayHandler() {
    try {
      addGET("/add/{Integer... numbers}/", "add");
      addGET("/add/{Double... numbers}/", "add");
      addGET("/add/{Integer one}/{Integer two}", "add");

      //addGET("/print/{String p}/{String ...}", "print");
      
      //addGET("/*", "nomath");
    }
    catch (HTTPException e) {
      e.printStackTrace();
      message(500, EXCEPTION_ERROR);
    }
  }

  /**
   * Add a list of numbers together.
   * @param numbers
   */
  public void add(Integer... numbers) {
    System.out.println("numbers:" + numbers);
    int answer = 0;
    String message = "";
    for(Integer i : numbers) {
      answer += i;
      if(message.isEmpty())
        message += i;
      else
        message += " + " + i;
    }
    message += " = " + answer;

    message(200, message);
  }

  /**
   * Add a list of numbers together.
   * @param numbers
   */
  public void add(Double... numbers) {
    double answer = 0;
    String message = "";
    for(Double i : numbers) {
      answer += i;
      if(message.isEmpty())
        message += i;
      else
        message += " + " + i;
    }
    message += " = " + answer;

    message(200, message);
  }

  /**
   * Adds two numbers together.
   * @param one
   * @param two
   */
  public void add(Integer one, Integer two) {
    message(200, one + " + " + two + " = "  + (one + two));
  }

  public void print(String p, String[] strings) {
    String message = p + " ";
    for(String s : strings) {
      message += s + " ";
    }

    message(200, message);
  }
  
  
  public void noMath() {
	  message(404, "No math...");
  }
}

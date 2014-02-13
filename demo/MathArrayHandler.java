package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;
import httpserver.HTTPResponse;

public class MathArrayHandler extends HTTPHandler {

  public MathArrayHandler() throws HTTPException {
    addGET("/add/{Integer... numbers}/", "add");
    addGET("/add/{Double... numbers}/", "add");
    //addGET("/add/{Integer one}/{Integer two}", "add");

    //addGET("/print/{String p}/{String ...}", "print");
    addGET("/*", "noMath");
  }

  /**
   * Add a list of numbers together.
   * @param numbers
   */
  public void add(HTTPResponse resp, Integer... numbers) {
    //System.out.println("numbers:" + numbers);
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

    resp.message(200, message);
  }

  /**
   * Add a list of numbers together.
   * @param numbers
   */
  public void add(HTTPResponse resp, Double... numbers) {
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

    resp.message(200, message);
  }
  

  public void print(HTTPResponse resp, String p, String[] strings) {
    String message = p + " ";
    for(String s : strings) {
      message += s + " ";
    }

    resp.message(200, message);
  }
  
  
  public void noMath(HTTPResponse resp, HTTPRequest req) {
	  resp.message(404, "No math...");
  }
}

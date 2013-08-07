package demo;

import httpserver.*;
import java.io.*;
import java.net.*;


public class HelloHandler extends HTTPHandler {

  public HelloHandler(Socket sock, HTTPRequest request) throws HTTPException {
    super(sock, request);

    try {
      addGET("/", "sayHello");
      addGET("/{String}/", "sayHello");
      addGET("/{String}/{String}", "sayHello");

      addGET("/goodbye", "sayGoodbye");
    } catch (HTTPException e) {
      e.printStackTrace();
    }
  }

  public void sayHello() {
    setResponseText("Hello World");
    setHandled(true);
  }

  public void sayHello(String firstName) {
    String response = "Hello " + firstName;
    setResponseText(response);
    setHandled(true);
  }

  public void sayHello(String firstName, String lastName) {
    String response = "Hello " + firstName + " " + lastName;
    setResponseText(response);
    setHandled(true);
  }

  public void sayGoodbye() {
    setResponseText("Goodbye World");
    setHandled(true);
  }
}
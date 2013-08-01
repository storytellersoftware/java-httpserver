import httpserver.*;

public class Driver {
  public static void main(String args[]) {
    HTTPServer s = new HTTPServer();

    // add some handlers to the HTTPServer
    s.addDefaultHandler(FileHandler.class);
    s.addHandler("plus", AdditionHandler.class);

    s.run();
  }
}
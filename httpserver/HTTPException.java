package httpserver;

public class HTTPException extends Exception {
  public HTTPException() {
    super();
  }

  public HTTPException(String message) {
    super(message);
  }
}
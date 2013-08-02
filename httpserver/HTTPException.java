package httpserver;

/**
 * An HTTPException is just a generic exception.
 *
 * We just use it when something bad happens with us...
 */
public class HTTPException extends Exception {
  public HTTPException() {
    super();
  }

  public HTTPException(String message) {
    super(message);
  }
}
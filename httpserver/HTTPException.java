package httpserver;

public class HTTPException extends Exception {
	public HTTPException() {
		super();
	}

	public HTTPException(String message) {
		super(message);
	}

	public HTTPException(String message, Exception e) {
		super(message, e);
	}
}
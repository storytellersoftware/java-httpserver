package httpserver;

/**
 * An HttpException is just a generic exception.
 *
 * We just use it when something bad happens with us...
 */
public class HttpException extends Exception {
    private static final long serialVersionUID = -1318922991257945983L;

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Exception e) {
        super(message, e);
    }

    public HttpException(Exception e) {
        super(e);
    }
}

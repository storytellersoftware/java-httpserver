package httpserver;

public interface Route {
    public void handle(HTTPRequest request, HTTPResponse response);
}

package httpserver;

public interface Route {
    public void handle(HttpRequest request, HttpResponse response);
}

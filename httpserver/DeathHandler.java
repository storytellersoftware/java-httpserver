package httpserver;

public class DeathHandler extends HTTPHandler {
  public DeathHandler(HTTPRequest request) {
    super(request);
  }

  public void handle() {
    message(500, "Well, that went well...");
  }
}
package handlers;

import httpserver.*;

public class AdditionHandler extends HTTPHandler {

  public AdditionHandler(HTTPRequest request) {
    super(request);
  }

  public void handle() {
    if (getRequest().getPath().size() >= 2) {
      int out = 0;

      for (int i = 1; i < getRequest().getPath().size(); i++) {
        out += Integer.parseInt(getRequest().getPath().get(i));
      }

      message(200, Integer.toString(out));
    }
    else {
      message(418, MALFORMED_INPUT_ERROR);
    }
  }
}
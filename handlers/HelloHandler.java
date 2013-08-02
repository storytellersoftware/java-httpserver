package handlers;

import httpserver.*;

public class HelloHandler extends HTTPHandler {

	public HelloHandler(HTTPRequest request) throws HTTPException {
		super(request);
		try {
			addPath("/", "sayHello");
			addPath("/{String}", "sayHelloF");
			addPath("/{String}/{String}", "sayHelloFL");

			/*addPath("/goodbye", "sayGoodbye");*/
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public void sayHello() {
		setResponseText("Hello World");
		setHandled(true);
	}

	public void sayHelloF() {
		String response = "Hello " + getRequest().getPath().get(1);
		setResponseText(response);
		setHandled(true);
	}

	public void sayHelloFL() {
		String response = "Hello " + getRequest().getPath().get(1) + " " + getRequest().getPath().get(2);
		setResponseText(response);
		setHandled(true);
	}

	public void sayGoodbye() {
		setResponseText("Goodbye World");
		setHandled(true);
	}
}
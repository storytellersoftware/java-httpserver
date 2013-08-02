package handlers;

import httpserver.*;

public class HelloHandler extends HTTPHandler {

	public HelloHandler(HTTPRequest request) {
		super(request);
		try {
			addPath("/hello", "sayHello");
			addPath("/hello/{String}", "sayHelloF");
			addPath("/hello/{String}/{String}", "sayHelloFL");

			addPath("/goodbye", "sayGoodbye");
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
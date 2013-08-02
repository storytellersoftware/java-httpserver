package handlers;

import httpserver.*;

public class HelloHandler extends HTTPHandler {

	public HelloHandler(HTTPRequest request) {
		super(request);
		try {
			addGET("/", "sayHello");
			addGET("/{String}/", "sayHelloF");
			addGET("/{String}/{String}", "sayHelloFL");

			addGET("/goodbye", "sayGoodbye");
		} catch (HTTPException e) {
			e.printStackTrace();
		}
	}

	public void sayHello() {
		setResponseText("Hello World");
		setHandled(true);
	}

	public void sayHelloF(String firstName) {
		String response = "Hello " + firstName;
		setResponseText(response);
		setHandled(true);
	}

	public void sayHelloFL(String firstName, String lastName) {
		String response = "Hello " + firstName + " " + lastName;
		setResponseText(response);
		setHandled(true);
	}

	public void sayGoodbye() {
		setResponseText("Goodbye World");
		setHandled(true);
	}
}
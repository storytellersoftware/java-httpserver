package demo;

import httpserver.*;

public class HelloHandler extends HTTPHandler {

	public HelloHandler(HTTPRequest request) throws HTTPException {
		super(request);

		try {
			addGET("/", "sayHello");
			addGET("/{String}/", "sayHello");
			addGET("/{String}/{String}", "sayHello");

			addGET("/goodbye", "sayGoodbye");
		} 
		catch (HTTPException e) {
			e.printStackTrace();
		}
	}

	public void sayHello() {
		setResponseText("Hello World");
		setHandled(true);
	}

	public void sayHello(String firstName) {
		String response = "Hello " + firstName;
		setResponseText(response);
		setHandled(true);
	}

	public void sayHello(String firstName, String lastName) {
		String response = "Hello " + firstName + " " + lastName;
		setResponseText(response);
		setHandled(true);
	}

	public void sayGoodbye() {
		setResponseText("Goodbye World");
		setHandled(true);
	}
}
package handlers;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

public class AddHandler extends HTTPHandler {

	public AddHandler(HTTPRequest request) {
		super(request);

		try {
			addGET("{Integer}/{Integer}", "add");
			addGET("{Float}/{Float}", "add");
		} catch (HTTPException e) {
			e.printStackTrace();
		}
	}

	public void add(Integer one, Integer two) {
		setResponseText(one + " + " + two + " = " + (one + two));
		setHandled(true);
	}

	public void add(Float one, Float two) {
		setResponseText(one + " + " + two + " = " + (one + two));
		setHandled(true);
	}

}

package httpserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * An HTTPHandler is what all handlers used by your server descend from.
 * The only real requirement for a handler is that it has a <code>handle</code>
 * method, where it sets the values that are going to be used by the
 * HTTPResponse.
 */
public abstract class HTTPHandler {
	public static final String EXCEPTION_ERROR = "an exception occured while processing your request";
	public static final String NOT_A_METHOD_ERROR = "No known method";
	public static final String MALFORMED_INPUT_ERROR = "Malformed Input";

	public static final String STATUS_GOOD = "All systems are go";

	public static HashMap<String, Method> methods = new HashMap<String, Method>();
	private Class<? extends HTTPHandler> handler;

	private HTTPRequest request;
	private int responseCode;
	private String responseType;
	private String responseText;
	private int responseSize;
	private boolean handled;

	/**
	 * Create an HTTPHandler.
	 * @param request HTTPRequest with the browser's request.
	 */
	public HTTPHandler(HTTPRequest request) {
		setRequest(request);
		handler = this.getClass();

		// default to good things
		setResponseCode(200);
		setResponseSize(-1);
		setHandled(false);
		setResponseType("text/plain");
	}


	public void handle() {
		
	}


	/**
	 * Where subclasses perform their specific actions.
	 * @throws HTTPException
	 */
	public void handleOld() throws HTTPException {
		String path = getRequest().getFullPath();

		try {
			Method method = methods.get(path);

			// If the method is null, there could be dynamic text in the url
			if (method == null) {
				// Iterate over the keys
				outerloop:
					for (String key : methods.keySet()) {
						// We need the index of '{' because it is the escape character for dynamic text
						int index = key.indexOf('{');

						// This will be manipulated based on the key
						String newPath = "";
						// If there is dynamic text
						if (index != -1) {
							// Check if the text before the '{' matches before we continue
							if (!path.substring(0, index).equalsIgnoreCase(key.substring(0, index)))
								index = -1;
							else
								newPath = path.substring(0, index-1);
						}
						// While we have a '{' and the path still matches what was there before
						while (index != -1 &&
								newPath.substring(0, index-1).equalsIgnoreCase(key.substring(0, index-1))) {
							// Add the next part of the dynamic text to the new path
							newPath += key.substring(index-1, key.indexOf('}', index) + 1);

							// Create another string that has newPath and the rest of the regular path to test with
							String testPath;
							if(path.indexOf('/', index) != -1)
								testPath = newPath + path.substring(path.indexOf('/', index));
							else
								testPath = newPath;

							// Check for our method
							method = methods.get(testPath);

							// If we have found a method, invoke it and get out of here
							if (method != null) {
								break outerloop;
							}
							// Set the new index to the next index of '{'
							index = key.indexOf('{', index + 1);
						}
					}
			}

			System.out.println("Method invoked: " + method);

			method.invoke(this);

		} catch (NullPointerException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			throw new HTTPException("Could not handle path: " + getRequest().getFullPath());
		}
	}

	// TODO allow parameters or not?
	/*public void addPath(String path, String methodName, Class<?> ... parameters) {
		try {
			// Add the method from the handler that added the path
			Method method = handler.getMethod(methodName, parameters);
			methods.put(path, method);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}*/
	

	/**
	 * Add a method to a path.
	 *
	 * Methods are passed in using "className#methodName" form so that
	 * we can parse out the correct Method. Who knows, you might want to
	 * use a method somewhere else, and who are we to argue with that?
	 *
	 * If no # is included, we assume it belongs to the class it's called in.
	 *
	 * @param path Path to match
	 * @param 	classMethod Class and Method in class#method form.
	 * @throws HTTPException When you do bad things.
	 */
	public void addPath(String path, String classMethod) throws HTTPException {
		//Class<? extends HTTPHandler> cl = null;
		Class<? extends HTTPHandler> cl;
		Method method = null;

		try {
			if (classMethod.indexOf('#') != -1) {
				String[] cm = classMethod.split("#");
				cl = (Class<? extends HTTPHandler>) Class.forName(cm[0]);
				method = cl.getMethod(cm[1]);
			}
			else {
				cl = this.getClass();
				method = cl.getMethod(classMethod);
			}

			methods.put(path, method);

		}
		catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new HTTPException("Not a class#method or method... " + classMethod);
		}
	}

	/**
	 * Send a simple string message with an HTTP response code back to
	 * the client. Can be used for sending all data back.
	 * @param code An HTTP response code.
	 * @param message The content of the server's response to the browser
	 */
	public void message(int code, String message) {
		setResponseCode(code);
		setResponseText(message);
		setHandled(true);
	}

	/**
	 * Tell the browser there is no response data.
	 * This is done by sending over a 204 code, which means there isn't
	 * any data in the stream, but the server correctly processed the request
	 */
	public void noContent() {
		setResponseCode(204);
		setResponseText("");
		setHandled(true);
	}

	/**
	 * Same as message(), but prints out an exception.
	 *
	 * @see httpserver.HTTPHandler#message
	 * @param code HTTP status code
	 * @param message the content being sent back to the browser
	 * @param t A throwable object, to be printed to the screen
	 */
	public void error(int code, String message, Throwable t) {
		t.printStackTrace();
		message(code, message);
	}

	public void setRequest(HTTPRequest request) {
		this.request = request;
	}
	public HTTPRequest getRequest() {
		return request;
	}

	public void setResponseCode(int code) {
		responseCode = code;
	}
	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseSize(int size) {
		responseSize = size;
	}
	public int getResponseSize() {
		return responseSize;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}
	public boolean isHandled() {
		return handled;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}
	public String getResponseText() {
		return responseText;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
	public String getResponseType() {
		return responseType;
	}
}
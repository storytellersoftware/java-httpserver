package httpserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private HashMap<String, MethodWrapper> getMethods = new HashMap<String, MethodWrapper>();
	private HashMap<String, MethodWrapper> postMethods = new HashMap<String, MethodWrapper>();
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


	public void handle() throws HTTPException {
		String path = getSimplePath(getRequest().getFullPath());

		HashMap<String, MethodWrapper> map = getMethodHash();
		invokeMethod(map, path);
	}

	private void invokeMethod(HashMap<String, MethodWrapper> map, String path) throws HTTPException {
		MethodWrapper method = map.get(path);
		List<Object> parameters = new ArrayList<Object>();

		if(method == null) {
			System.out.println(map.keySet());
		}

		method.invoke(this, parameters);
	}

	private String getSimplePath(String path) {
		System.out.println("Full Path: " + path);
		if(path.split("/").length >= 2)
			path = path.substring(path.indexOf('/', 1), path.length());
		System.out.println("Path: " + path);

		return path;
	}

	/**
	 * Where subclasses perform their specific actions.
	 * @throws HTTPException
	 */
	@Deprecated
	public void handleOld() throws HTTPException {
		String path = getRequest().getFullPath();
		System.out.println("Full Path: " + path);
		if(path.charAt(path.length() - 1) != '/')
			path += "/";
		if(path.split("/").length != 2)
			path = path.substring(path.indexOf('/', 1), path.length());
		path = path.toLowerCase();
		System.out.println("Path: " + path);
		HashMap<String, MethodWrapper> methods;
		if(getRequest().getRequestType().equalsIgnoreCase(HTTPRequest.GET_REQUEST_TYPE))
			methods = getMethods;
		else
			methods = postMethods;

		try {
			MethodWrapper method = methods.get(path);
			List<Object> parameters = new ArrayList<Object>();

			// If the method is null, there could be dynamic text in the url
			if (method == null) {
				// Iterate over the keys
				outerloop:
					for (String key : methods.keySet()) {
						parameters = new ArrayList<Object>();
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

			System.out.println("Method invoked: " + method + "\n");

			method.invoke(this, parameters);

		} catch (NullPointerException | IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			throw new HTTPException("Could not handle path: " + getRequest().getFullPath());
		}
	}

	private HashMap<String, MethodWrapper> getMethodHash() {
		if(getRequest().getRequestType().equals(HTTPRequest.GET_REQUEST_TYPE))
			return getMethods;
		else
			return postMethods;
	}

	public void addGET(String path, String methodName) throws HTTPException {
		addMethod(getMethods, path, methodName);
	}

	public void addPOST(String path, String methodName) throws HTTPException {
		addMethod(postMethods, path, methodName);
	}

	private void addMethod(HashMap<String, MethodWrapper> map, String path, String methodName) throws HTTPException {
		MethodWrapper method = new MethodWrapper(path, methodName, this.getClass());
		map.put(path, method);
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
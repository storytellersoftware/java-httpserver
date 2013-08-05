package httpserver;

import java.util.HashMap;
import java.util.Set;

/**
 * An HTTPHandler is what all handlers used by your server descend from.
 *
 * Extended classes have two options for determining their actions: they may
 * override the handle method (slightly harder), or use the addGet and addPost
 * methods in the constructor. See their descriptions for more information.
 *
 * @see HTTPHandler#handle
 * @see HTTPHandler#addGet
 * @see HTTPHandler#addPost
 */
public abstract class HTTPHandler {
	/** Generic error message for when an exception occurs on the server */
	public static final String EXCEPTION_ERROR
	= "an exception occured while processing your request";

	/** Generic error message for when there isn't a method assigned to the
					requested path */
	public static final String NOT_A_METHOD_ERROR = "No known method";

	/** Generic error message for when the browser sends bad data */
	public static final String MALFORMED_INPUT_ERROR = "Malformed Input";

	/** Generic status message for when everything is good */
	public static final String STATUS_GOOD = "All systems are go";

	private HashMap<String, MethodWrapper> getMethods = new HashMap<String, MethodWrapper>();
	private HashMap<String, MethodWrapper> postMethods = new HashMap<String, MethodWrapper>();

	private HTTPRequest request;
	private int responseCode;
	private String responseType;
	private String responseText;
	private int responseSize;
	private boolean handled;

	/**
	 * Create an HTTPHandler.
	 *
	 * This also sets some acceptable defaults:
	 *		The response code is set to 200 (OK, which means everything happend
	 *		all nice and good-like);
	 *
	 *		The response size is set to -1, which tells the HTTPResponse to
	 *		determine the correct size when sends back the information;
	 *
	 *		The response is told it hasn't been handled yet;
	 *
	 *		And the response mimetype is set to "text/plain".
	 *
	 * @param request HTTPRequest with the browser's request.
	 * @see HTTPResponse
	 * @see HTTPHandler#setResponseCode
	 * @see HTTPHandler#setResponseSize
	 * @see HTTPHandler#setHandled
	 * @see HTTPHandler#setResponseType
	 */
	public HTTPHandler(HTTPRequest request) {
		setRequest(request);

		// default to good things
		setResponseCode(200);
		setResponseSize(-1);
		setHandled(false);
		setResponseType("text/plain");
	}


	/**
	 * Where the Handler handles the information given from the request and based off of the paths
	 * specified in the Handler.
	 * This can be overridden for a more custom handling.
	 * @throws HTTPException
	 */
	public void handle() throws HTTPException {
		String path = getRequest().getPath();
		System.out.println("PathAS: " + path);
		MethodWrapper method = getMap().get(path);

		int mostCorrect = 0;

		if(method == null) {
			// Find the most correct method
			Set<String> keys = getMap().keySet();
			for(String key : keys) {
				MethodWrapper testMethod = getMap().get(key);
				if(testMethod.howCorrect(path) > mostCorrect)
					method = testMethod;
			}
		}

		method.invoke(this, path);
	}

	/**
	 * Where subclasses perform their specific actions.
	 * @throws HTTPException
	 * @Deprecated use .handle() instead
	 */
	@Deprecated
	public void handleOld() throws HTTPException {
		String path = getRequest().getFullPath();
		System.out.println("Full Path: " + path);

		if(path.charAt(path.length() - 1) != '/')
			path += "/";

		if (path.split("/").length != 2) {
			path = path.substring(path.indexOf('/', 1), path.length());
			path = path.toLowerCase();
		}

		System.out.println("Path: " + path);

		HashMap<String, MethodWrapper> methods;

		if(getRequest().getRequestType().equalsIgnoreCase(HTTPRequest.GET_REQUEST_TYPE))
			methods = getMethods;
		else
			methods = postMethods;

		try {
			MethodWrapper method = methods.get(path);

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

			method.invoke(this, path);

		} catch (NullPointerException | IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			throw new HTTPException("Could not handle path: " + getRequest().getFullPath());
		}
	}

	/**
	 * Gets the correct Map of Methods the request wants to use.
	 * @return The HashMap for the correct request.
	 */
	private HashMap<String, MethodWrapper> getMap() {
		if(getRequest().getRequestType().equals(HTTPRequest.GET_REQUEST_TYPE))
			return getMethods;
		else
			return postMethods;
	}

	/**
	 * Attach a method to a GET request at a path.
	 *
	 * Methods are passed in using "className#methodName" form so that
	 * we can parse out the correct Method. Who knows, you might want to
	 * use a method somewhere else, and who are we to argue with that?
	 *
	 * If no # is included, we assume it belongs to the class it's called in.
	 *
	 * Path's should come in "/path/to/action" form. If the method requires
	 * parameters, they should be included in the path, in the order they're
	 * listed in the method definition, but in "{ClassName}" form. Example:
	 * <code>/hello/{String}/{String}</code> is a good path.
	 *
	 * Methods being used should <strong>only</strong> have parameters that are
	 * included in the java.lang library. Any other type of parameter will cause
	 * an exception to occur.
	 *
	 * Additionally, primitives are not permited, because they're not classes in
	 * the java.lang library.
	 *
	 * @param path         Path to match
	 * @param classMethod 	Class and Method in class#method form.
	 * @throws HTTPException When you do bad things.
	 *
	 * @see HTTPHandler#addPOST
	 */
	public void addGET(String path, String methodName) throws HTTPException {
		addMethod(getMethods, path, methodName);
	}

	/**
	 * Attach a method to a POST request at a path.
	 *
	 * For a more detailed explanation, see addGET.
	 *
	 * @param path         Path to match
	 * @param classMethod 	Class and Method in class#method form.
	 * @throws HTTPException When you do bad things.
	 *
	 * @see HTTPHandler#addGET
	 */
	public void addPOST(String path, String methodName) throws HTTPException {
		addMethod(postMethods, path, methodName);
	}


	/**
	 * Add a method to a path in a map.
	 *
	 * Methods are passed in using "className#methodName" form so that
	 * we can parse out the correct Method. Who knows, you might want to
	 * use a method somewhere else, and who are we to argue with that?
	 *
	 * If no # is included, we assume it belongs to the class it's called in.
	 *
	 * @param map             The map to add this junks to.
	 * @param path            Path to match
	 * @param classMethod     Class and Method in class#method form.
	 *
	 * @throws HTTPException  When you do bad things.
	 */
	private void addMethod(HashMap<String, MethodWrapper> map, String path, String methodName) throws HTTPException {
		MethodWrapper method = new MethodWrapper(path, methodName, this.getClass());
		map.put(path, method);
	}

	/**
	 * Send a simple string message with an HTTP response code back to
	 * the client.
	 *
	 * Can be used for sending all data back.
	 *
	 * @param code      An HTTP response code.
	 * @param message   The content of the server's response to the browser
	 *
	 * @see HTTPHandler#error
	 * @see HTTPHandler#noContent
	 */
	public void message(int code, String message) {
		setResponseCode(code);
		setResponseText(message);
		setHandled(true);
	}

	/**
	 * Tell the browser there is no response data.
	 *
	 * This is done by sending over a 204 code, which means there isn't
	 * any data in the stream, but the server correctly processed the request
	 *
	 * @see HTTPHandler#message
	 */
	public void noContent() {
		setResponseCode(204);
		setResponseText("");
		setHandled(true);
	}

	/**
	 * Send a message to the browser and print an exception
	 *
	 * Prints the stackTrace of `t`, and sends a message `message` back to the
	 * browser, with that HTTP status of `code`
	 * 
	 * @param code      HTTP status code
	 * @param message   the content being sent back to the browser
	 * @param t         A throwable object, to be printed to the screen
	 * 
	 * @see httpserver.HTTPHandler#message
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
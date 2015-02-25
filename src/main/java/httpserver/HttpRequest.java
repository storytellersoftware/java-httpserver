package httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HttpRequest takes an incoming connection and parses out all of the
 * relevant data, supposing the connection follows HTTP protocol.
 *
 * At present, HttpRequest only knows how to handle HTTP 1.1 requests, and
 * doesn't handle persistent connections. Technically, it could handle an
 * HTTP 1.0 request, because 1.0 doesn't have persistent connections.
 *
 * @see   <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">
 *        HTTP 1.1 Spec</a>
 * @see HttpHandler
 */
public class HttpRequest implements Runnable {
    /** HTTP GET request type */
    public static final String GET_REQUEST_TYPE = "GET";

    /** HTTP POST request type */
    public static final String POST_REQUEST_TYPE = "POST";

    /** HTTP HEAD request type */
    public static final String HEAD_REQUEST_TYPE = "HEAD";

    /** HTTP DELETE request type */
    public static final String DELETE_REQUEST_TYPE = "DELETE";

    /** HTTP PUT request type */
    public static final String PUT_REQUEST_TYPE = "PUT";


    // used to determine what one does with the request
    private HttpRouter router;

    // connection with client
    private Socket connection;

    // the handler used to determine what the server actually does
    // with this request
    private HttpHandler handler;

    // the full text of the incoming request, including headers
    // and sent over data
    private String httpRequest;

    // the request line, or first line of entire request
    private String requestLine;

    // the type of request, as in GET, POST, ...
    private String requestType;

    // the protocol the client is using
    private String requestProtocol;

    // All headers, because they're all key/value pairs
    private Map<String, String> headers = new HashMap<>();

    // The requested path, split by '/'
    private List<String> splitPath = new ArrayList<>();

    // The path relative to the handler's path
    private String path;

    // the full path
    private String fullPath;

    // the POST data
    private Map<String, String> params = new HashMap<>();

    private List<String> varargs = new ArrayList<>();


    /**
     * Used to parse out an HTTP request provided a Socket and figure out the
     * handler to be used.
     *
     * @param connection The socket between the server and client
     * @throws IOException      When it gets thrown by
     *                          {@link HttpRequest#parseRequest}.
     * @throws SocketException  When it gets thrown by
     *                          {@link HttpRequest#parseRequest}.
     * @throws HttpException    When something that doesn't follow HTTP spec
     *                          occurs.
     *
     * @see HttpRequest#parseRequest
     */
    public HttpRequest(HttpRouter router, Socket connection) throws IOException, SocketException, HttpException {
        this.router = router;
        connection.setKeepAlive(true);
        setConnection(connection);
    }

    @Override
    public void run() {
        if (getConnection().isClosed()) {
            System.out.println("Socket is closed...");
        }

        try {
            createResponse().respond();
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse createResponse() throws IOException, HttpException {
        parseRequest();
        HttpResponse response = new HttpResponse(this);
        determineHandler().handle(this, response);

        return response;
    }


    /**
     * Kicks off the request's parsing. Called inside constructor.
     *
     * @throws IOException      When an InputStream can't be retreived from the
     *                          socket.
     * @throws SocketException  When the client breaks early. This is a browser
     *                          issue, and not a server issue, but it gets thrown
     *                          upstream because it can't be dealt with until it
     *                          gets to the HttpServer.
     * @throws HttpException    When headers aren't in key/value pairs separated
     *                          by ": ".
     *
     * @see HttpServer
     */
    public void parseRequest() throws IOException, SocketException, HttpException {
        // Used to read in from the socket
        BufferedReader input = new BufferedReader(
                new InputStreamReader(getConnection().getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();

        /*  The HTTP spec (Section 4.1) says that a blank first line should be
            ignored, and that the next line SHOULD have the request line. To be
            extra sure, all initial blank lines are discarded.
            */
        String firstLine = input.readLine();
        if (firstLine == null) {
            throw new HttpException("Input is returning nulls...");
        }

        while (firstLine.isEmpty()) {
            firstLine = input.readLine();
        }

        // start with the first non-empty line.
        setRequestLine(firstLine);
        requestBuilder.append(getRequestLine());
        requestBuilder.append("\n");

        /*  Every line after the first, but before an empty line is a header,
            which is a key/value pair.

            The key is before the ": ", the value, after

            TODO: parse this to spec. Spec says it's cool to have any number of
            whitespace characters following the colon, and the values
            can be spread accross multiple lines provided each following line
            starts with a whitespace character.

            For more information, see issue 12 and RFC 2616#4.2.
            Issue 12: https://github.com/dkuntz2/java-httpserver/issues/12
            RFC 2616#4.2: http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
            */
        for (String line = input.readLine(); line != null && !line.isEmpty(); line = input.readLine()) {
            requestBuilder.append(line);
            requestBuilder.append("\n");

            String[] items = line.split(": ");

            if (items.length == 1) {
                throw new HttpException("No key value pair in \n\t" + line);
            }

            String value = items[1];
            for (int i = 2; i < items.length; i++) {
                value += ": " + items[i];
            }

            getHeaders().put(items[0], value);
        }


        /*  If the client sent over a POST request, there's *probably* still data
            in the stream. This reads in only the number of chars specified in the
            "Content-Length" header.
            */
        if (getRequestType().equals(POST_REQUEST_TYPE) && getHeaders().containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(getHeaders().get("Content-Length"));
            StringBuilder b = new StringBuilder();

            for (int i = 0; i < contentLength; i++) {
                b.append((char)input.read());
            }

            requestBuilder.append(b.toString());

            String[] data = b.toString().split("&");
            getParams().putAll(parseInputData(data));
        }

        setHttpRequest(requestBuilder.toString());
    }


    /**
     * Turns an array of "key=value" strings into a map. <p>
     *
     * Any item in the array missing an "=" is given a value of null.
     *
     * @param data  List of strings in "key=value" form, you know, like HTTP GET
     *              or POST lines?
     * @return  Map of key value pairs
     */
    private Map<String, String> parseInputData(String[] data) {
        Map<String, String> out = new HashMap<String, String>();
        for (String item : data) {
            if (item.indexOf("=") == -1) {
                out.put(item, null);
                continue;
            }

            String value = item.substring(item.indexOf('=') + 1);

            /*  Attempt to URL decode the value, because it *might* be user input.
                If it can't be decoded, it doesn't matter, the original, undecoded
                value is still used.
                */
            try {
                value = URLDecoder.decode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {}

            out.put(item.substring(0, item.indexOf('=')), value);
        }

        return out;
    }

    /**
     * Figure out what kind of HttpHandler you want, based on the path. <p>
     *
     * This uses the statically set {@link HttpRouter} to determine the
     * correct HttpHandler to be used for the current request. If there isn't
     * a statically set HttpRouter, a 500 error is sent back to the
     * client.
     *
     * @return a new instance of some form of HttpHandler.
     *
     * @see HttpRouter
     * @see HttpRouter#determineHandler
     * @see HttpHandler
     */
    public HttpHandler determineHandler() {
        if (router == null) {
            return new DeathHandler();
        }

        String path = getSplitPath().isEmpty() ? "" : getSplitPath().get(0);
        return router.route(path, this);
    }

    /**
     * Return if the request type is the passed in type.
     * @param requestTypeCheck The type to check.
     * @return whether the request type equals the passed in String.
     */
    public boolean isType(String requestTypeCheck) {
        return getRequestType().equalsIgnoreCase(requestTypeCheck);
    }

    /**
     * Sets the requestLine, and all derived items. <p>
     *
     * Based off of the passed in line, the request type, request path, and
     * request protocol can be set.
     *
     * @param line  The first line in an HTTP request. Should be in
     *              {@code [type] [full path] [protocol]} form.
     * @throws HttpException  When the first line does not contain two spaces,
     *                        signifying that the passed in line is not in
     *                        HTTP 1.1. When the type is not an expected type
     *                        (currently GET, POST, and HEAD).
     *
     * @see HttpRequest#setRequestType
     * @see HttpRequest#setFullPath
     * @see HttpRequest#setRequestProtocol
     */
    public void setRequestLine(String line) throws HttpException {
        this.requestLine = line;

        /*  Split apart the request line by spaces, as per the protocol.
            The request line should be:
            [request type] [path] [protocol]
            */
        String[] splitty = requestLine.trim().split(" ");
        if (splitty.length != 3) {
            throw new HttpException("Request line has a number of spaces other than 3.");
        }


        // Set the request type
        setRequestType(splitty[0].toUpperCase());

        // set the path
        setFullPath(splitty[1]);

        // set the protocol type
        setRequestProtocol(splitty[2]);
    }
    /**
     * Return the request line.
     * @return  the request line.
     */
    public String getRequestLine() {
        return requestLine;
    }


    /**
     * Set the full path, and path list. <p>
     *
     * Because the path list is derived from the full path, it's set at the same
     * time.
     *
     * @param inPath  The full requested path (in `/path/to/request` form)
     *
     * @see HttpRequest#setPath
     * @see HttpRequest#setSplitPath
     */
    public void setFullPath(String inPath) {
        this.fullPath = inPath;
        setPath(inPath);
        setSplitPath(inPath);
    }
    /**
     * Gets the full path of the request.
     * @return The full path.
     */
    public String getFullPath() {
        return fullPath;
    }

    public void setPath(String path) {
        this.path = path;
    }
    /**
     * Gets the path relative to the handler's path.
     * @return Everything in the path after the handler's path.
     */
    public String getPath() {
        return path;
    }


    /**
     * Given a full path, set the splitPath to the path, split by `/`. <p>
     *
     * If there's a query string attached to the path, it gets removed from the
     * splitPath, and the request's associated GET data is parsed from the query
     * string.
     *
     * @see HttpRequest#getGetData
     */
    public void setSplitPath(String fullPath) {
        /*  Split apart the path for future reference by the handlers
            The split path should be used by handlers to figure out what
            action should be taken. It's also used to parse out GET request
            data.

            The first character *should* always be a `/`, and that could cause
            an error with splitting (as in, the first split could be an empty
            string, which we don't want).
            */
        for (String segment : fullPath.substring(1).split("/")) {
            if (segment.isEmpty()) {
                continue;
            }

            getSplitPath().add(segment);
        }

        if (getSplitPath().isEmpty()) {
            return;
        }

        /*  Parse out any GET data in the request URL.
            This could occur on any request.
            */
        if (getSplitPath().get(getSplitPath().size() - 1).indexOf('?') != -1) {
            String lastItem = getSplitPath().get(getSplitPath().size() - 1);
            // remove the ? onward from the last item in the path, because that's not
            // part of the requested URL
            getSplitPath().set(getSplitPath().size() - 1, lastItem.substring(0,
                        lastItem.indexOf('?')));

            // split apart the request query into an array of "key=value" strings.
            String[] data = lastItem.substring(lastItem.indexOf('?') + 1).split("&");

            // Set the GET data to the GET data...
            getParams().putAll(parseInputData(data));
        }
    }
    public void setSplitPath(List<String> path) {
        this.splitPath = path;
    }
    /**
     * Gets the path relative to the handler's path split by '/'
     * @return A List of Strings
     */
    public List<String> getSplitPath() {
        return splitPath;
    }



    public void setConnection(Socket connection) {
        this.connection = connection;
    }
    public Socket getConnection() {
        return connection;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setParams(Map<String, String> data) {
        this.params = data;
    }
    public Map<String, String> getParams() {
        return params;
    }
    public void mergeParams(Map<String, String> data) {
        this.params.putAll(data);
    }
    public String getParam(String key) {
        return this.params.get(key);
    }

    public void mergeVarargs(List<String> data) {
        this.varargs.addAll(data);
    }
    public List<String> getVarargs() {
        return this.varargs;
    }

    public void setHttpRequest(String httpRequest) {
        this.httpRequest = httpRequest;
    }
    public String getHttpRequest() {
        return httpRequest;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public String getRequestType() {
        return requestType;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }
    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }
    public HttpHandler getHandler() {
        return handler;
    }

    public void setRouter(HttpRouter router) {
        this.router = router;
    }
    public HttpRouter getRouter() {
        return router;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpRequest from ");
        builder.append(getConnection().getLocalAddress().getHostAddress());
        builder.append("\n\t");
        builder.append("Request Line: ");
        builder.append(getRequestLine());
        builder.append("\n\t\t");
        builder.append("Request Type ");
        builder.append(getRequestType());
        builder.append("\n\t\t");
        builder.append("Request Path ");
        builder.append(getFullPath());

        return builder.toString();
    }
}

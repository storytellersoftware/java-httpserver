package httpserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * An HttpResponse is used to set output values, and to write those values
 * to the client.
 */
public class HttpResponse {
    /** Generic error message for when an exception occurs on the server */
    public static final String EXCEPTION_ERROR
        = "an exception occurred while processing your request";

    /** Generic error message for when there isn't a method assigned to the requested path */
    public static final String NOT_A_METHOD_ERROR = "No known method";

    /** Generic error message for when the browser sends bad data */
    public static final String MALFORMED_INPUT_ERROR = "Malformed Input";

    /** Generic status message for when everything is good */
    public static final String STATUS_GOOD = "All systems are go";

    private static String serverInfo;
    private static Map<Integer, String> responses;

    private HttpRequest request;

    private int code = 200; // default to "200 - OK"
    private byte[] body;
    private String mimeType = "text/plain";
    private long size = -1;

    private Map<String, String> headers = new HashMap<>();

    private Socket socket;
    private DataOutputStream writer;


    /**
     * Create a new HttpResponse to fill out. <p>
     *
     * It defaults to sending a {@code text/plain} type document, with
     * a status of {@code 200 Ok}, with a body of nothing.
     */
    public HttpResponse(HttpRequest req) throws IOException {
        if (getServerInfo() == null || getServerInfo().isEmpty()) {
            setupServerInfo();
        }

        socket = req.getConnection();
        writer = new DataOutputStream(socket.getOutputStream());

        request = req;
    }


    /**
     * Send a simple string message with an HTTP response code back to
     * the client. <p>
     *
     * Can be used for sending all data back.
     *
     * @param code      An HTTP response code.
     * @param message   The content of the server's response to the browser
     *
     * @see HttpResponse#error
     * @see HttpResponse#noContent
     */
    public void message(int code, String message) {
        setCode(code);
        setBody(message);
        setMimeType("text/plain");
    }


    /**
     * Tell the browser there is no response data. <p>
     *
     * This is done by sending over a 204 code, which means there isn't
     * any data in the stream, but the server correctly processed the request
     *
     * @see HttpResponse#message
     */
    public void noContent() {
        setCode(204);
        setBody("");
        setMimeType("");
    }


    /**
     * Send a message to the browser and print an exception<p>
     *
     * Prints the stackTrace of `t`, and sends a message `message` back to the
     * browser, with that HTTP status of `code`
     *
     * @param code      HTTP status code
     * @param message   the content being sent back to the browser
     * @param t         A throwable object, to be printed to the screen
     *
     * @see HttpResponse#message
     */
    public void error(int code, String message, Throwable t) {
        t.printStackTrace();
        message(code, message);
    }


    /**
     * Send data back to the client.
     */
    public void respond() {
        try {
            // If the socket doesn't exist, or is null, we have a small problem.
            // Because no data can be written to the client (there's no way to
            // talk to the client), we need to get out of here, let the user
            // know something janky is going on, and stop trying to do things.
            //
            // Thankfully that can all be done by throwing an exception.
            if (getSocket() == null) {
                throw new HttpException("Socket is null...");
            } else if (getSocket().isClosed()) {
                throw new HttpException("Socket is closed...");
            }


            // If the user never filled out the response's body, there isn't any
            // content. Make sure the response code matches that.
            if(getBody() == null) {
                noContent();
            }

            // Send the required headers down the pipe.
            writeLine("HTTP/1.1 " + getResponseCodeMessage(getCode()));
            writeLine("Server: " + getServerInfo());
            writeLine("Content-Type: " + getMimeType());

            writeLine("Connection: close");

            if (getSize() != -1) {
                // Someone manually set the size of the body. Go team!
                writeLine("Content-Size: " + getSize());
            } else {
                // We don't know how large the body is. Determine that using the body...
                writeLine("Content-Size: " + getBody().length);
            }

            // Send all other miscellaneous headers down the shoots.
            if (!getHeaders().isEmpty()) {
                StringBuilder b = new StringBuilder();
                for (String key : getHeaders().keySet()) {
                    b.append(key);
                    b.append(": ");
                    b.append(getHeader(key));
                    b.append("\n");
                }
                writeLine(b.toString());
            }

            // Blank line separating headers from the body.
            writeLine("");

            // If there isn't a body, or the client made a HEAD request, stop
            // doing things.
            if (getRequest().isType(HttpRequest.HEAD_REQUEST_TYPE) || getCode() == 204) {
                return;
            }

            // Give the client the body.
            getWriter().write(getBody());
        } catch (HttpException | IOException e) {
            System.err.println("Something bad happened while trying to send data "
                    + "to the client");
            e.printStackTrace();
        } finally {
            try {
                getWriter().close();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes a string and a "\n" to the DataOutputStream.
     * @param line The line to write
     * @throws IOException
     */
    protected void writeLine(String line) throws IOException {
        getWriter().writeBytes(line + "\n");
    }


    /*********************
      GETTERS AND SETTERS
     *********************/

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }


    public byte[] getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body.getBytes();
    }
    public void setBody(byte[] bytes) {
        body = bytes;
    }


    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        if (size < 0) {
            throw new RuntimeException("Response Content-Length must be non-negative.");
        }

        this.size = size;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getHeader(String key) {
        return headers.get(key);
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }


    private HttpRequest getRequest() {
        return request;
    }


    private Socket getSocket() {
        return socket;
    }
    private DataOutputStream getWriter() {
        return writer;
    }


    /**
     * Return the response code + the response message.
     *
     * @see HttpHandler#getResponseCode
     * @see HttpHandler#setResponseCode
     */
    public static String getResponseCodeMessage(int code) {
        if (responses == null || responses.isEmpty()) {
            setupResponses();
        }

        if (responses.containsKey(code)) {
            return code + " " + responses.get(code);
        }

        return Integer.toString(code);
    }

    /**************
      STATIC STUFF
     **************/

    /**
     * Sets up a list of response codes and text.
     */
    private static void setupResponses() {
        responses = new HashMap<Integer, String>();

        responses.put(100, "Continue");
        responses.put(101, "Switching Protocols");

        responses.put(200, "OK");
        responses.put(201, "Created");
        responses.put(202, "Accepted");
        responses.put(203, "Non-Authoritative Information");
        responses.put(204, "No Content");
        responses.put(205, "Reset Content");
        responses.put(206, "Partial Content");

        responses.put(300, "Multiple Choices");
        responses.put(301, "Moved Permanently");
        responses.put(302, "Found");
        responses.put(303, "See Other");
        responses.put(304, "Not Modified");
        responses.put(305, "Use Proxy");
        responses.put(307, "Temporary Redirect");

        responses.put(400, "Bad Request");
        responses.put(401, "Unauthorized");
        responses.put(402, "Payment Required");
        responses.put(403, "Forbidden");
        responses.put(404, "Not Found");
        responses.put(405, "Method Not Allowed");
        responses.put(406, "Not Acceptable");
        responses.put(407, "Proxy Authentication Required");
        responses.put(408, "Request Timeout");
        responses.put(409, "Conflict");
        responses.put(410, "Gone");
        responses.put(411, "Length Required");
        responses.put(412, "Precondition Failed");
        responses.put(413, "Request Entity Too Large");
        responses.put(414, "Request-URI Too Long");
        responses.put(415, "Unsupported Media Type");
        responses.put(416, "Request Range Not Satisfiable");
        responses.put(417, "Expectation Failed");
        responses.put(418, "I'm a teapot");
        responses.put(420, "Enhance Your Calm");

        responses.put(500, "Internal Server Error");
        responses.put(501, "Not implemented");
        responses.put(502, "Bad Gateway");
        responses.put(503, "Service Unavaliable");
        responses.put(504, "Gateway Timeout");
        responses.put(505, "HTTP Version Not Supported");
    }


    /**
     * Set the info of the server
     */
    public void setupServerInfo() {
        StringBuilder info = new StringBuilder();
        info.append(HttpServer.getServerName());
        info.append(" v");
        info.append(HttpServer.getServerVersion());
        info.append(" (");
        info.append(HttpServer.getServerETC());
        info.append(")");
        setServerInfo(info.toString());
    }
    public static void setServerInfo(String serverInfo) {
        HttpResponse.serverInfo = serverInfo;
    }
    public static String getServerInfo() {
        return serverInfo;
    }
}

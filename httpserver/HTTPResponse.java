package httpserver;

import java.util.*;
import java.net.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;


public class HTTPResponse {
  private static Map<Integer, String> responses
  = new HashMap<Integer, String>();
  public static String serverInfo;

  private HTTPHandler handler;
  private Socket socket;
  private DataOutputStream writer;

  public HTTPResponse(Socket socket, HTTPHandler handler) throws IOException,
  HTTPException {
    if (responses.isEmpty()) {
      setupDefaultResponses();
    }

    if (getServerInfo() == null || getServerInfo().isEmpty()) {
      StringBuilder info = new StringBuilder();
      info.append(HTTPServer.SERVER_NAME);
      info.append(" v");
      info.append(HTTPServer.SERVER_VERSION);
      info.append(" (");
      info.append(HTTPServer.SERVER_ETC);
      info.append(")");
      setServerInfo(info.toString());
    }

    setHandler(handler);
    setSocket(socket);

    setWriter(new DataOutputStream(getSocket().getOutputStream()));

    respond();
    getWriter().close();
  }

  public void respond() throws IOException, HTTPException {
    getHandler().handle();

    writeLine("HTTP/1.1 " + getResponseCode(getHandler().getResponseCode()));
    writeLine("Server: " + getServerInfo());
    writeLine("Content-Type: " + getHandler().getResponseType());

    // persistant connection not currently supported
    writeLine("Connection: close");

    if (getHandler().getResponseSize() != -1) {
      writeLine("Content-Size: " + getHandler().getResponseSize());
    }
    else {
      writeLine("Content-Size: " + getHandler().getResponseText().length());
    }

    writeLine("");

    /*  If the browser is using a HEAD request, it only wants the headers,
        and all subsequent content can be ignored.

        Alternatively, if the handler responds with a code of 204, there
        isn't any content to be sent over, so let's just stop trying, okay?
     */
    if (getHandler().getRequest().isType(HTTPRequest.HEAD_REQUEST_TYPE)
        || getHandler().getResponseCode() == 204) {
      return;
    }

    if(isImage())
      imageRepsonse();
    else
      regularResponse();
  }

  private void regularResponse() throws IOException {
    writeLine(getHandler().getResponseText());
  }

  private void imageRepsonse() throws IOException {
    System.out.println(getHandler().getResponseText());
    String imgType = getHandler().getResponseType().substring(getHandler().getResponseType().length() - 3);
    BufferedImage img = ImageIO.read(new URL(getHandler().getResponseText()).openStream());

    ImageIO.write(img, imgType, getWriter());

  }

  private boolean isImage() {
    return getHandler().getResponseType().contains("image");
  }

  private void writeLine(String line) throws IOException {
    getWriter().writeBytes(line + "\n");
  }


  public void setupDefaultResponses() {
    responses.put(200, "OK");
    responses.put(201, "Created");
    responses.put(204, "No content");

    responses.put(404, "Not Found");
    responses.put(418, "I'm a teapot");

    responses.put(500, "Internal Server Error");
    responses.put(501, "Not implemented");
  }

  public String getResponseCode(int responseCode) {
    if (responses.containsKey(responseCode)) {
      return responseCode + " " + responses.get(responseCode);
    }

    return Integer.toString(responseCode);
  }


  public static void setServerInfo(String info) {
    serverInfo = info;
  }
  public static String getServerInfo() {
    return serverInfo;
  }

  public void setHandler(HTTPHandler handler) {
    this.handler = handler;
  }
  public HTTPHandler getHandler() {
    return handler;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }
  public Socket getSocket() {
    return socket;
  }

  public void setWriter(DataOutputStream writer) {
    this.writer = writer;
  }
  public DataOutputStream getWriter() {
    return writer;
  }

}
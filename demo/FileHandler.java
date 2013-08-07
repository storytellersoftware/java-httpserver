package demo;

import java.io.*;
import java.net.*;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

public class FileHandler extends HTTPHandler {

  private static final String CONTENT_DIRECTORY = "javadoc";
  private static String defaultFile;


  /**
   * Create a FileHandler.
   */
  public FileHandler(HTTPRequest request) {
    super(request);
    setDefaultFile("index.html");
  }

  /**
   * Set the response data to be a file on the server.
   *
   * @throws HTTPException  When an IOException occurs, either because the
   *                        the requested file doesn't exist, or there are
   *                        problems reading the file.
   * @Override
   */
  @Override
  public void handle() throws HTTPException {
    try {
      StringBuilder pathBuilder = new StringBuilder();
      for (String segment : getRequest().getSplitPath()) {
        pathBuilder.append("/");
        pathBuilder.append(segment);
      }

      String path = pathBuilder.toString();
      if (path.isEmpty()) {
        path = "/";
      }

      if(path.substring(path.length() - 1).equals("/")) {
        path += defaultFile;
      }

      path = CONTENT_DIRECTORY + path;

      // Set the response type
      if (path.substring(path.length() - 4).equalsIgnoreCase("html")) {
        setResponseType("text/html");
      }
      else if (path.substring(path.length() - 3).equalsIgnoreCase("css")) {
        setResponseType("text/css");
      }
      else if (path.substring(path.length() - 3).equalsIgnoreCase("gif")) {
        setResponseType("image/gif");
      }


      if (isImage()) {
        setResponseText("file://" + getResource(path));
        setResponseSize(new File(
                        new URL(getResponseText()).toString()).length());

        return;
      }

      InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);

      // If the file wasn't found, alert the user that it was not found
      if (inputStream == null) {
        message(404, "<h1>404 - File Not Found</h1>");
        return;
      }

      BufferedReader bufferedReader = new BufferedReader(
                      new InputStreamReader(inputStream));
      StringBuilder builder = new StringBuilder();

      for (String line = bufferedReader.readLine(); line != null;
                      line = bufferedReader.readLine()) {
        builder.append(line);
        builder.append("\n");
      }

      bufferedReader.close();

      setResponseText(builder.toString());
    }
    catch (IOException e) {
      throw new HTTPException("File Not Found", e);
    }
  }

  private boolean isImage() {
    return getResponseType().contains("image");
  }

  public static void setDefaultFile(String path) {
    defaultFile = "/" + path;
  }

  /**
   * Gets an absolute path from a relative path
   *
   * @param path The relative path of a resource
   * @return The relative path's absolute path
   */
  public static String getResource(String path) {
    try {
      return URLDecoder.decode(
                      ClassLoader.getSystemClassLoader().getResource(
                                      URLDecoder.decode(path, "UTF-8")).getPath(), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // This won't happen...
      e.printStackTrace();
    }

    return ClassLoader.getSystemClassLoader().getResource(path).getPath();
  }

}

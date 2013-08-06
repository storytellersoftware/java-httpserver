package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

public class FileHandler extends HTTPHandler {

  private static final String CONTENT_DIRECTORY = "javadoc";
  private static String defaultFile;

  public FileHandler(HTTPRequest request) {
    super(request);
    setDefaultFile("index.html");
  }

  @Override
  public void handle() throws HTTPException {
    try {
      // Get the path
      String path;
      if(getRequest().getPath().indexOf('?') != -1)
        path = getRequest().getPath().substring(0, getRequest().getPath().indexOf('?'));
      else
        path = getRequest().getPath();
      if(path.equals("/"))
        path = defaultFile;
      path = CONTENT_DIRECTORY + path;

      // Set the response type
      if(path.substring(path.length() - 4).equalsIgnoreCase("html"))
        setResponseType("text/html");
      else if(path.substring(path.length() - 3).equalsIgnoreCase("css"))
        setResponseType("text/css");
      else if(path.substring(path.length() - 3).equalsIgnoreCase("gif"))
        setResponseType("image/gif");

      if(isImage()) {
        setResponseText("file://" + getResource(path));
        setResponseSize(new File(new URL(getResponseText()).toString()).length());
      }

      else {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);

        // If the file wasn't found, alert the user that it was not found
        if(inputStream == null) {
          message(404, "<h1>404 - File Not Found</h1>");
        }

        // Read the file and send it to the user
        else {
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
          StringBuilder builder = new StringBuilder();

          for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            builder.append(line);
            builder.append("\n");
          }

          bufferedReader.close();

          setResponseText(builder.toString());
        }
      }

    } catch(IOException e) {
      throw new HTTPException("File Not Found", e);
    }
  }

  private boolean isImage() {
    return getResponseType().contains("image");
  }

  public static void setDefaultFile(String path) {
    defaultFile = "/" + path;
  }
}

package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

public class FileHandlerTest extends HTTPHandler {

  public String contentDirectory = "demo/www";
  public String defaultFile = "index.html";

  public FileHandlerTest(HTTPRequest request) throws HTTPException {
    super(request);

    setDefaultFile("index.html");
    setContentDirectory("demo/www");

    addGET("{String... paths}", "getFile");
    addGET("/", "getFile");
  }

  /**
   * Gets the default file.
   * 
   * @throws HTTPException
   *           If the file doesn't exist
   */
  public void getFile() {
    getFile((String[]) null);
  }

  /**
   * Gets a file based on the paths array that the client requested.
   * 
   * @param paths
   *          - The paths the client requested.
   * @throws HTTPException
   *           If the file doesn't exist.
   */
  public void getFile(String... paths) {
    try {
      // Create a String Builder to put our path together.
      StringBuilder pathBuilder = new StringBuilder(getContentDirectory());

      // If paths is not null, we can (probably) serve a file.
      if (paths != null) {
        for (String segment : paths) {
          pathBuilder.append("/");
          pathBuilder.append(segment);
        }
      }
      // If paths is null, we will serve the default file.
      else {
        pathBuilder.append("/");
        pathBuilder.append(getDefaultFile());
      }

      // Our path is complete.
      String path = pathBuilder.toString();

      // Sets our response type.
      setContentType(path);

      // If it is an image request, we have to handle it differently.
      if (isImageResponse()) {
        // Set the response text to our image's location.
        setResponseText("file://" + getResource(path));
        return;
      }

      // If it is a normal file, we can read it here.
      InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
              inputStream));

      StringBuilder read = new StringBuilder();
      for (String line = bufferedReader.readLine(); line != null; line =
              bufferedReader.readLine()) {
        read.append(line);
        read.append("\n");
      }
      bufferedReader.close();

      // Set the response text to our file's data.
      setResponseText(read.toString());

    } catch (IOException | NullPointerException e) {
      // Tell the client the file was not found.
      message(404, "<h1>404 - File Not Found</h1>");

      // We can't throw this due to reflection, but we can at least print
      // the stack trace to tell the developer something bad happened.
      new HTTPException("File Not Found", e).printStackTrace();
    }
  }

  /**
   * Sets the content type that is being sent to the client.
   * 
   * @param path
   *          - The path the client is requesting.
   */
  private void setContentType(String path) {
    if (path.substring(path.length() - "html".length())
            .equalsIgnoreCase("html")) {
      setResponseType("text/html");
    }
    else if (path.substring(path.length() - "css".length()).equalsIgnoreCase(
            "css")) {
      setResponseType("text/css");
    }
    else if (path.substring(path.length() - "gif".length()).equalsIgnoreCase(
            "gif")) {
      setResponseType("image/gif");
    }
    else if (path.substring(path.length() - "jpg".length()).equalsIgnoreCase(
            "jpg")) {
      setResponseType("image/jpg");
    }
  }

  /**
   * Gets an absolute path from a relative path
   * 
   * @param path
   *          The relative path of a resource
   * @return The relative path's absolute path
   */
  public static String getResource(String path) {
    try {
      return URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(
              URLDecoder.decode(path, "UTF-8")).getPath(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // This won't happen...
      e.printStackTrace();
    }

    return ClassLoader.getSystemClassLoader().getResource(path).getPath();
  }

  /**
   * Checks if an image will be returned to the client.
   * 
   * @return whether the response type is an image type.
   */
  private boolean isImageResponse() {
    return getResponseType().contains("image");
  }

  /**
   * This must be overridden because images are different than regular files.
   * This will send the requested file back to the client.
   */
  @Override
  public void writeData() throws IOException {
    // If we have an image response, read it, then write it.
    if (isImageResponse()) {
      String imgType = getResponseType().substring(
              getResponseType().length() - 3);

      BufferedImage img = ImageIO.read(
              new URL(getResponseText()).openStream());

      ImageIO.write(img, imgType, getWriter());
    }
    // If we have a regluar file, just write it.
    else {
      writeLine(getResponseText());
    }
  }

  /**
   * Gets the content directory of all files.
   * 
   * @return The files content directory.
   */
  public String getContentDirectory() {
    return contentDirectory;
  }

  /**
   * Sets the content directory of all files.
   * 
   * @param contentDirectory
   *          - Where all files are stored.
   */
  public void setContentDirectory(String contentDirectory) {
    // We do not want to include a '/' in front.
    if (contentDirectory.startsWith("/"))
      contentDirectory = contentDirectory.substring(1);
    this.contentDirectory = contentDirectory;
  }

  /**
   * Gets the path of the default file, whose path is '/'
   * 
   * @return The path of the default file.
   */
  public String getDefaultFile() {
    return defaultFile;
  }

  /**
   * Sets the path to the file that will be returned if the path is '/'
   * 
   * @param defaultFile
   *          - The path to the default file.
   */
  public void setDefaultFile(String defaultFile) {
    this.defaultFile = defaultFile;
  }

}

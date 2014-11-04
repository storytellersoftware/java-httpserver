package demo;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPResponse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler extends HTTPHandler {

  public static final int BUFFER_SIZE = 8192;

  public String contentDirectory = "demo/www";
  public String defaultFile = "index.html";

  public String path;

  public FileHandler() throws HTTPException {
    addGET("/", "serveDefaultFile");
    addGET("{String... paths}", "serveFile");
  }

  /**
   * Serve the default file.
   */
  public void serveDefaultFile(HTTPResponse resp) {
    // Serve the default file.
    serveFile(resp, getDefaultFile());
  }

  /**
   * Serve a file.
   * @param paths - The path to a file.
   */
  public void serveFile(HTTPResponse resp, String... paths) {	
    // Create a StringBuilder to build our path.
    StringBuilder pathBuilder = new StringBuilder(getContentDirectory());
		
    // Build the path.
    for (String segment : paths) {
      pathBuilder.append("/");
      pathBuilder.append(segment);
    }
		
    // Now our path is complete.
    path = pathBuilder.toString();
    System.out.println(path);

    // Setup header data for the file.
    if(new File(getResource(path)).exists()) {
      try {
    		byte[] bytes = Files.readAllBytes(Paths.get(getResource(path)));
    		resp.setMimeType(getContentType(path));
    		resp.setBody(bytes);
      } catch (IOException e) {
    	  // TODO Auto-generated catch block
    	  e.printStackTrace();
      }
    }
    // If it doesn't exist, let the user know.
    else {
      resp.setMimeType("text/html");
      resp.setCode(404);
      resp.setBody("404 - File Not Found!");
    }
  }

  
  /**
   * Sets the content type that is being sent to the client.
   * 
   * @param path - The path the client is requesting.
   */
  private String getContentType(String path) {
    String type = "";
    try {
      type = Files.probeContentType(FileSystems.getDefault().getPath(
        getResource(path)));
    } catch (IOException e) {
      // Should never happen.
      e.printStackTrace();
      type = "text/plain";
    }
    return type;
  }
  
  /**
   * Gets an absolute path from a relative path
   * 
   * @param path - The relative path of a resource
   * @return The relative path's absolute path
   */
  public static String getResource(String path) {
    try {
      return URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(
        URLDecoder.decode(path, "UTF-8")).getPath(), "UTF-8");
    } catch (UnsupportedEncodingException | NullPointerException e) {
      // This will only happen if the file doesn't exist and is handled later.
      return "";
    }
  }

  /**
   * Sets the content directory of all files.
   * 
   * @param contentDirectory - Where all files are stored.
   */
  public void setContentDirectory(String dir) {
    contentDirectory = dir;
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
   * Set the default file path.<p>
   * NOTE: You should not include the '/' before the path
   * @param pathToFile -  The path.
   */
  public void setDefaultFile(String pathToFile) {
    defaultFile = "/" + pathToFile;
  }
	
  /**
   * Gets the path of the default file, whose path is '/'
   * 
   * @return The path of the default file.
   */
  public String getDefaultFile() {
    return defaultFile;
  }

}

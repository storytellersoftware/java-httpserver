package demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

public class FileHandler extends HTTPHandler {
	
	public static final int BUFFER_SIZE = 8192;

	public String contentDirectory = "demo/www";
	public String defaultFile = "index.html";
	
	public String path;

	public FileHandler(HTTPRequest request) throws HTTPException {
		super(request);

		addGET("/", "serveDefaultFile");
		addGET("{String... paths}", "serveFile");
	}

	/**
	 * Serve the default file.
	 */
	public void serveDefaultFile() {
		// Serve the default file.
		serveFile(getDefaultFile());
	}

	/**
	 * Serve a file.
	 * @param paths - The path to a file.
	 */
	public void serveFile(String... paths) {	
		// Create a StringBuilder to build our path.
		StringBuilder pathBuilder = 
				new StringBuilder(getContentDirectory());
		
		// Build the path.
		for (String segment : paths) {
			pathBuilder.append("/");
			pathBuilder.append(segment);
		}
		
		// Now our path is complete.
		path = pathBuilder.toString();
		File file = new File(getResource(path));
		System.out.println(path);
		
		// Setup header data for the file.
		if(file.exists()) {
			setContentType(path);
			setResponseSize(new File(getResource(path)).length());
		}
		// If it doesn't exist, let the user know.
		else {
			setResponseType("text/html");
			setResponseCode(404);
		}
		
		// Do this so we don't return no content.
		setResponseText(""); // TODO This seems kinda hacky to me.
	}
	
	@Override
	public void writeData() throws IOException {
		try {
			// Write the data back in binary form.
			byte[] buffer = new byte[BUFFER_SIZE];
			InputStream in = ClassLoader.getSystemResourceAsStream(path);
			int read = 0;
			while((read = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				getWriter().write(buffer, 0, read);
			}
		} catch(Exception e) {
			// If we couldn't send them data, we probably don't have the file.
			System.err.println("File Not Found - " + path);
			writeLine("<h1>404 - File Not Found</h1>");
		} 
	}

	/**
	 * Sets the content type that is being sent to the client.
	 * 
	 * @param path - The path the client is requesting.
	 */
  private void setContentType(String path) {
	    try {
	      setResponseType(Files.probeContentType(
	      		FileSystems.getDefault().getPath(getResource(path))));
      } catch (IOException e) {
      	// Should never happen.
	      e.printStackTrace();
      }
  }
  
  /**
   * Gets an absolute path from a relative path
   * 
   * @param path - The relative path of a resource
   * @return The relative path's absolute path
   */
  public static String getResource(String path) {
    try {
      return URLDecoder.decode(
      		ClassLoader.getSystemClassLoader().getResource(
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

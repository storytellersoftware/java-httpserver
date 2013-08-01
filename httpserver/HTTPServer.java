package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * HTTPServer is a relatively simple class with one job, and one job only:
 * wait for incoming connections, and send the connections over to an
 * HTTPRequest and an HTTPResponse.
 *
 * An HTTPServer is not required to use the rest of the httpserver classes,
 * and might not be the best base server for one to use. It exists solely to
 * provide an existing mechanism for using the rest of the httpserver package.
 */
public class HTTPServer implements Runnable {
  /** The port being listend on */
  public static final int PORT = 8000;
  
  /** The server's name */
  public static final String SERVER_NAME = "Simple Java Server";
  
  /** The server's version */
  public static final String SERVER_VERSION = "0.0.1";

  /** Extra information about the server */
  public static final String SERVER_ETC = "now in Glorious Extra Color";


  private ServerSocket socket = null;

  /**
   * Tell the server to run! On the port specified in PORT
   */
  public void run() {
    try {
      socket = new ServerSocket();

      System.out.println("Starting HTTPServer at http://127.0.0.1:" + PORT);

      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(PORT));

      while (true) {
        Socket connection = null;
        try {
          connection = socket.accept();

          HTTPRequest request = new HTTPRequest(connection);
          System.out.println(request);
          //HTTPResponse response = new HTTPResponse(connection, request.getHandler());
        }
        catch (SocketException e) {
          /*  This typically occurs when the client breaks the connection, and
              isn't an issue on the server side, which means we shouldn't break
          */
          System.out.println("The client probably broke the connection early");
          e.printStackTrace();
        }
        catch (IOException e) {
          /*  This typically means there's a problem in the HTTPRequest
          */
          System.out.println("IOException. Probably an issue in the HTTPRequest");
          e.printStackTrace();
        }
        catch (Exception e) {
          /*  Some kind of unexpected exception occurred, something bad might
              have happened.
          */
          System.out.println("Generic Exception!");
          e.printStackTrace();

          /*  If you're currently developing using this, you might want to leave
              this break here, because this means something unexpected occured.
              If the break is left in, the server stops running, and you should
              probably look into the exception.

              If you're in production, you shouldn't have this break here,
              because you probably don't want to kill the server...
          */
          break;
        }
        finally {
          connection.close();
        }
      }
    }
    catch (Exception e) {
      /*  Not sure when this occurs, but it might...
      */
      System.out.println("Something bad happened...");
      e.printStackTrace();
    }
    finally {
      try {
        socket.close();
      }
      catch (IOException e) {
    	System.out.println("Well that's not good...");
    	e.printStackTrace();
      }
    }
  }

}
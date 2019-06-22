import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

/**
 * This program creates a server on port 50505 which listens for file requests. It handles errors to  
 * prevent the server from crashing due to bad requests. It also provides headers regarding the transfer.
 */

public class ReadRequest {
	
	/**
	 * The server listens on this port.
	 */
	private final static int LISTENING_PORT = 50505;
	
	/**
	 * main() starts the server and handleConnection()
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(LISTENING_PORT);
		}
		catch (Exception e) {
			System.out.println("Failed to create listening socket.");
			return;
		}
		System.out.println("Listening on port " + LISTENING_PORT);
		try {
			while (true) {
				Socket connection = serverSocket.accept();
				System.out.println("\nConnection from " 
						+ connection.getRemoteSocketAddress());
				handleConnection(connection);
			}
		}
		catch (Exception e) {
			System.out.println("Server socket shut down unexpectedly!");
			System.out.println("Error: " + e);
			System.out.println("Exiting.");
		}
	}

	/**
	 * handleConnection() checks if the request is proper, the file is available and handles the errors
	 * @param the client-server connection
	 */
	private static void handleConnection(Socket connection) {
		try {
			DataInputStream input = new DataInputStream(connection.getInputStream()); //user's input
			String rootDirectory="/root/Desktop";	//where files are located
			String request = input.readLine();	//String from getInputStream
			StringTokenizer requestTokens = new StringTokenizer(request);  //Tokenized request
			PrintWriter out = new PrintWriter(connection.getOutputStream(), true);  //server's output
			out.println(request);
			if (!requestTokens.nextToken().equals("GET"))
				handleConnection(connection);
			String requestedFile = requestTokens.nextToken();	//name of file user requested
			File myfile = new File(rootDirectory + requestedFile);  //file to find on server
			sendFile(myfile, connection.getOutputStream());
			if (myfile.canRead()) {
				out.println("HTTP/1.1 200 OK" + "\r\n");
				out.println("Content-type: " + getMimeType(requestedFile) );
				out.println("Content-Length: " + (int) myfile.length());
				out.println("Connection: close " + "\r\n");
				
			} 
			else {
				out.println("Error" + "\r\n");
			}
		}
		catch (Exception e) {
			System.out.println("Error while communicating with client: " + e);
		}
		finally {  // make SURE connection is closed before returning!
			try {
				connection.close();
			}
			catch (Exception e) {
			}
			System.out.println("Connection closed.");
		}
	}

	/**
	 * getMimeType() provides the info for the header dealing with the type of file being transferred
	 * @param the file user requested
	 */
	private static String getMimeType(String fileName) {
		int pos = fileName.lastIndexOf('.');
		if (pos < 0) // no file extension in name
			return "x-application/x-unknown";
		String ext = fileName.substring(pos+1).toLowerCase();
		if (ext.equals("txt")) return "text/plain";
		else if (ext.equals("html")) return "text/html";
		else if (ext.equals("htm")) return "text/html";
		else if (ext.equals("css")) return "text/css";
		else if (ext.equals("js")) return "text/javascript";
		else if (ext.equals("java")) return "text/x-java";
		else if (ext.equals("jpeg")) return "image/jpeg";
		else if (ext.equals("jpg")) return "image/jpeg";
		else if (ext.equals("png")) return "image/png";
		else if (ext.equals("gif")) return "image/gif";
		else if (ext.equals("ico")) return "image/x-icon";
		else if (ext.equals("class")) return "application/java-vm";
		else if (ext.equals("jar")) return "application/java-archive";
		else if (ext.equals("zip")) return "application/zip";
		else if (ext.equals("xml")) return "application/xml";
		else if (ext.equals("xhtml")) return"application/xhtml+xml";
		else return "x-application/x-unknown";
		// Note: x-application/x-unknown is something made up;
		// it will probably make the browser offer to save the file.
	}

	/**
	 * sends the file
	 * @param the file user requested and the output stream
	 */
	private static void sendFile(File file, OutputStream socketOut) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		OutputStream out = new BufferedOutputStream(socketOut);
		while (true) {
			int x = in.read(); // read one byte from file
			if (x < 0)
				break; // end of file reached
			out.write(x); // write the byte to the socket
		}
		out.flush();
	}
}

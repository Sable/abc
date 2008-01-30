//Listing 7.15 EchoClient.java

import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
	    System.out.println(
			       "Usage: java EchoClient <server> <portNum>");
	    System.exit(1);
	}
	String serverName = args[0];
	int portNum = Integer.parseInt(args[1]);
	setup(serverName, portNum);
    }

    private static void setup(String serverName, int portNum)
	throws IOException {
	Socket requestSocket
	    = new Socket(InetAddress.getByName(serverName),
			 portNum);
	
	BufferedReader consoleReader
	    = new BufferedReader(new InputStreamReader(System.in));
	BufferedReader responseReader = new BufferedReader(
		   new InputStreamReader(requestSocket.getInputStream()));
	PrintWriter requestWriter
	    = new PrintWriter(requestSocket.getOutputStream());

	while(true) {
	    String requestString = consoleReader.readLine();
	    if (requestString.equals("quit")) {
		break;
	    }
	    requestWriter.println(requestString);
	    requestWriter.flush();
	    System.out.println("Response: "
			       + responseReader.readLine());
	}
	requestWriter.close();
	responseReader.close();
	requestSocket.close();
    }
}

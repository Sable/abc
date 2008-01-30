//Listing 7.9 EchoServer.java

import java.io.*;
import java.net.*;

public class EchoServer {
    public static void main(String[] args) throws Exception {
	if (args.length != 1) {
	    System.out.println("Usage: java EchoServer <portNum>");
	    System.exit(1);
	}
	int portNum = Integer.parseInt(args[0]);
	ServerSocket serverSocket = new ServerSocket(portNum);
	while(true) {
	    Socket requestSocket = serverSocket.accept();
	    Runnable worker = new EchoWorker(requestSocket);
	    Thread serverThread = new Thread(worker);
	    serverThread.start();
	}
    }
}

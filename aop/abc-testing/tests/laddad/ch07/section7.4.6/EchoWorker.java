//Listing 7.10 EchoWorker.java: the worker class

import java.io.*;
import java.net.*;

class EchoWorker implements Runnable {
    private Socket _requestSocket;

    public EchoWorker(Socket requestSocket) throws IOException {
	_requestSocket = requestSocket;
    }

    public void run() {
	BufferedReader requestReader = null;
	PrintWriter responseWriter = null;
	try {
	    requestReader
		= new BufferedReader(new InputStreamReader(
				   _requestSocket.getInputStream()));
	    responseWriter
		= new PrintWriter(_requestSocket.getOutputStream());
	    while(true) {
		String requestString = requestReader.readLine();
		if (requestString == null) {
		    break;
		}
		System.out.println("Got request: " + requestString);
		responseWriter.write(requestString + "\n");
		responseWriter.flush();
	    }
	} catch (IOException ex) {
	} finally {
	    try {
		if(requestReader != null) {
		    requestReader.close();
		}
		if(responseWriter != null) {
		    responseWriter.close();
		}
		_requestSocket.close();
	    } catch (IOException ex2) { }
	}
	System.out.println("Ending the session");
    }
}


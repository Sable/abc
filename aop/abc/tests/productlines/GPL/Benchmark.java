// Benchmark.java aspect
// GPL using AspectJ
// Roberto E. Lopez-Herrejon
// Product-Line Architecture Research Lab
// Department of Computer Sciences
// University of Texas at Austin
// Last update: May 3, 2002

package GPL;

import  java.util.LinkedList;
import java.io.*;

public aspect Benchmark 
{
    // ****************************************************************
    // **** Graph
    public Reader Graph.inFile;               // File handler for reading
    public static int Graph.ch;               // Character to read/write
	
    // timings
    static long Graph.last=0; 
    static long Graph.current=0; 
    static long Graph.accum=0;
      
    public void GPL.Graph.runBenchmark(String FileName) throws IOException {
	try {
	    inFile = new FileReader(FileName);
	} catch (IOException e) {
	    System.out.println("Your file " + FileName + " cannot be read");
	}		  
    }
	  
    public void Graph.stopBenchmark() throws IOException {
	inFile.close();
    }
	  
    public int Graph.readNumber() throws IOException {
  	int index =0;
	char[] word = new char[80];
	int ch=0;
	
	ch = inFile.read();
	while(ch==32) ch = inFile.read();  // skips extra whitespaces
	
	while(ch!=-1 && ch!=32 && ch!=10) // while it is not EOF, WS, NL
	{
	    word[index++] = (char)ch;
	    ch = inFile.read();
	}
	word[index]=0;
	
	String theString = new String(word);
	
	theString = new String(theString.substring(0,index));
	return Integer.parseInt(theString,10);
    }
    
    public static void Graph.startProfile() {
	accum = 0;
	current = System.currentTimeMillis();
	last = current;
    }
	  
    public static void Graph.stopProfile() {
	current = System.currentTimeMillis();
	accum = accum + (current - last);
    }
	  
    public static void Graph.resumeProfile() {
	current = System.currentTimeMillis();
	last = current;
    }
	  
    public static void Graph.endProfile() {
	current = System.currentTimeMillis();
	accum = accum + (current-last);
	System.out.println("Time elapsed: " + accum + " milliseconds");
    }

}

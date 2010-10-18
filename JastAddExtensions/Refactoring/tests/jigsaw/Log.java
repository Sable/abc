package tests.jigsaw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class Log {
	String path;
	private Collection<LogEntry> entries = new LinkedList<LogEntry>();
	private BufferedWriter writer;
	
	public Log(String path) throws IOException{
		FileWriter filewriter = new FileWriter(path);
		writer = new BufferedWriter(filewriter);
		writer.write(LogEntry.header()+System.getProperty("line.separator"));
	}
	
	public void add(LogEntry logEntry) throws IOException {
		entries.add(logEntry);
		writer.write(logEntry.toString()+System.getProperty("line.separator"));
		writer.flush();
		System.out.println(logEntry);
		if(size() %10 == 0)
			print();
	}
	
	public void done() throws IOException {
		writer.close();
		System.out.println("done.");
	}

	public int size() {
		return entries.size();
	}

	public void print() {
		int successes = 0;
		int exceptions = 0;
		int timeouts = 0;
		int errors = 0;
		long duration = 0;
		for (LogEntry entry : entries) {
			if(entry.wasSuccess())
				successes++;
			if(entry.hasException())
				exceptions++;
			if(entry.timeout())  
				timeouts++;
			else
				duration = entry.duration();
			if(entry.hasErrors())
				errors++;
		}
		System.out.println();
		System.out.println("runs:              " + entries.size());
		System.out.println("successes:         " + successes);
		System.out.println("exceptions:        " + exceptions);
		System.out.println("timeouts:          " + timeouts);
		System.out.println("errors:            " + errors);
		System.out.println("average duration:  " + ((entries.size()-timeouts) == 0 ? "--" : (duration / (entries.size()-timeouts))));
		System.out.println();	
	}
}

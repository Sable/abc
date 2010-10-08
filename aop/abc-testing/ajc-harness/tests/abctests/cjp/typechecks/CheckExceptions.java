import java.io.*;

public aspect CheckExceptions {
	
	joinpoint void JPNone();
	
	joinpoint void JPException() throws Exception;

	joinpoint void JPIOException() throws IOException;
	
	before JPNone() throws Exception { } //error: cannot throw Exception
	
	void around JPIOException() throws Exception { } //error: cannot throw Exception 

	void around JPException() throws IOException { } 	//ok
}
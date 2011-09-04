import java.io.*;

public aspect CheckExceptions {
	
	jpi void JPNone();
	
	jpi void JPException() throws Exception;

	jpi void JPIOException() throws IOException;
	
	before JPNone() throws Exception { } //error: cannot throw Exception
	
	void around JPIOException() throws Exception { } //error: cannot throw Exception 

	void around JPException() throws IOException { } 	//ok
}
import java.io.*;

public aspect CheckExceptions2 {
	
	void nothing() {
		exhibit JPNone {};	//ok
	}
	
	void throwIOException() throws IOException{
		exhibit JPIOException {}; //ok		
	}

	void uncaught() {
		exhibit JPException {}; //error: must catch Exception		
	}

	void uncaught2() throws IOException {
		exhibit JPException {}; //error: must catch Exception		
	}

	joinpoint void JPNone();
	
	joinpoint void JPException() throws Exception;

	joinpoint void JPIOException() throws IOException;
}
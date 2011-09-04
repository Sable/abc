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

	jpi void JPNone();
	
	jpi void JPException() throws Exception;

	jpi void JPIOException() throws IOException;
}
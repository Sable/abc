import java.io.IOException;
import java.io.InterruptedIOException;

class AException extends Exception{}
class BException extends Exception{}

jpi void JP() throws IOException;
<E extends IOException> jpi void JP1() throws E;
<E extends Exception> jpi void JP2() throws E, IOException;

aspect A{
	
	void around JP() throws IOException{
		throw new Exception(); //error
	}

	<E extends IOException> void around JP1() throws E{
		throw new Exception(); //error
	}
	<E extends IOException> void around JP1() throws E{
		proceed();//ok
	}

	<E extends Exception> void around JP2() throws E{
		throw new Exception(); //error
	}
	<E extends Exception> void around JP2() throws IOException{
		throw new Exception(); //error
	}
	<E extends Exception> void around JP2() throws E, IOException{
		throw new AException(); //error
	}
	<E extends Exception> void around JP2() throws E, IOException{
		throw new IOException(); //ok
	}	
}
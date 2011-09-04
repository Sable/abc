import java.io.*;
import java.util.*;

public aspect CheckAfterThrowing {
	
	jpi String JPIO() throws IOException;

	jpi String JPAny() throws IOException, Exception;

	jpi List JPUSO() throws UnsupportedOperationException;

	after JPIO() throwing(Exception e) {} //ok: types are cast-convertible
	
	after JPUSO() throwing(RuntimeException e) {} //ok: exception type not a checked exception

	after JPAny() throwing(UnsupportedOperationException e) {} //ok: Exception cast-convertible to UnsupportedOperationException 
	
	after JPUSO() throwing(IOException e) {} //error: checked exception and types are not cast-convertible
}
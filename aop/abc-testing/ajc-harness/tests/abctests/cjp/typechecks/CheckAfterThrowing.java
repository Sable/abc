import java.io.*;
import java.util.*;

public aspect CheckAfterThrowing {
	
	joinpoint String JPIO() throws IOException;

	joinpoint String JPAny() throws IOException, Exception;

	joinpoint List JPUSO() throws UnsupportedOperationException;

	after JPIO() throwing(Exception e) {} //ok: types are cast-convertible
	
	after JPUSO() throwing(RuntimeException e) {} //ok: exception type not a checked exception

	after JPAny() throwing(UnsupportedOperationException e) {} //ok: Exception cast-convertible to UnsupportedOperationException 
	
	after JPUSO() throwing(IOException e) {} //error: checked exception and types are not cast-convertible
}
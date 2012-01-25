/**
 * jpi doesn't indicate an exception, but the advice related with it yes. 
 * Through an static analysis is it possible say that the program is safe. Is it possible?
 */

import java.io.*;

jpi void JP() throws EOFException, FileNotFoundException;

aspect A{
	
	void around JP() throws EOFException, FileNotFoundException{ //ok
		proceed(); //ok
	}
 
    void around JP(){ //ok
    	try{
    		proceed(); //ok
    	}
    	catch(FileNotFoundException e){
    		throw new FileNotFoundException(); //error
    	}
    	catch(EOFException e){
    		throw new EOFException(); //error
    	}
    }    
}




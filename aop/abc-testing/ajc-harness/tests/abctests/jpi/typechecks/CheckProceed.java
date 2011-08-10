/**
 * proceed
 */

import java.io.*;

jpi void JP() throws Exception;
jpi void JP2() throws Exception;
jpi void JP3() throws Exception;

aspect A{

    void around JP() throws IOException{
    	proceed(); //error, can't raise Exception
    }
    
    void around JP2() throws IOException{
    	try{
    		proceed(); //error
    	}
    	catch(IOException e){ //only catches IOExceptions, not the Exception raised by proceed
    		throw new IOException();
    	}
    	proceed(); //error
    }

    void around JP3() throws IOException{
    	try{
    		proceed(); //ok
    	}
    	catch(Exception e){
    		throw new IOException();
    	}
    }

}

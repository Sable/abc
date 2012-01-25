/**
 * jpi doesn't indicate an exception, but the advice related with it yes. 
 * Through an static analysis is it possible say that the program is safe. Is it possible?
 */

import java.io.*;

jpi void JP();
jpi void JP2() throws Exception;
jpi void JP3() throws IOException;

aspect A{

    void around JP(){ //ok
        proceed(); //ok
    }
    
    void around JP() throws IOException{ // error: JP can not throw exceptions.
    	proceed(); //ok
    }

    void around JP2(){ //ok
    	try{
    		proceed(); //ok, catch handles Exception
    	}
    	catch (Exception e){}
    }
    
    void around JP2() { //ok
    	try{
    		proceed(); //error, catch doesn't handle Exception
    	}
    	catch (IOException e){}
    }    
    
    void around JP2() throws IOException{  //ok IOException <: Exception
    	proceed(); //error, can't raise Exception
    }
    
    void around JP2() throws IOException{ //ok
    	try{
    		proceed(); //
    	}
    	catch (Exception e){}
    	throw new IOException(); //ok
    }

    void around JP3() throws Exception{ //error, Exception not allowed for JP5
    	System.out.println("");
    }
    
}
import java.io.*;



jpi void JP(int ) throws EOFException, FileNotFoundException;

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




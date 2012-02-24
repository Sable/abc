import java.io.IOException;
import java.io.InterruptedIOException;

jpi void JP() throws IOException;
<E extends IOException> jpi void JP1() throws E;
<E extends Exception> jpi void JP2() throws E, IOException;

aspect A{
	
	void around JP(){}//ok
	void around JP() throws IOException{}//ok
	void around JP() throws IOException, InterruptedIOException{} //ok
	void around JP() throws Exception{} //error	
	void around JP() throws IOException, Exception{} //error	
	void around JP(){
		proceed();//error
	}
	void around JP() throws IOException{
		proceed();
	}
	void around JP(){
		try{
			proceed();//ok
		}
		catch(InterruptedIOException e){}
		catch(IOException e){}
	}
	
	void around JP1(){} //ok
	void around JP1() throws Exception{} //error
	<L extends Exception> void around JP1() throws L{}//error
	<L extends IOException> void around JP1() throws L{}//ok
	void around JP1(){
		proceed();//error
	}
	void around JP1(){
		try{
			proceed();//ok
		}
		catch(IOException e){}
	}
	
	void around JP2(){} //ok
	void around JP2() throws Exception{} //error
	void around JP2() throws IOException{} //ok	
	<L extends IOException> void around JP2() throws L{}//ok
	<L extends Exception> void around JP2() throws L{}//ok
	<L extends Exception> void around JP2() throws L, Exception{}//error
	<L extends Exception> void around JP2() throws L, IOException{}//ok
	void around JP2(){
		proceed();//error
	}
	void around JP2(){
		try{
			proceed();//error
		}
		catch(IOException e){}
	}
	
	void around JP2(){
		try{
			proceed();//ok
		}
		catch(IOException e){}
		catch(Exception e){}		
	}
	

	
}
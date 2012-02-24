import java.io.IOException;
import org.aspectj.testing.Tester;

<R extends Exception>jpi void JP() throws R, IOException;

public class C{
	
	public static int counter=0;

	<L extends Exception> exhibits void JP(): execution(* bar(..)); 
    
    public static void bar() throws IOException, Exception{}
    public static void main(String[] args){
    	try{
    		bar();
    	}
    	catch(Exception e){}
    	Tester.checkEqual(counter,3,"expected 3 matches but saw "+counter);     	
    }
}


aspect A{
	
	<I extends Exception> void around JP(){
		C.counter++;		
		try{
			proceed();
		}
		catch(IOException i){}
		catch(Exception e){}
	}

	<I extends Exception> void around JP() throws I, IOException{
		C.counter++;		
		proceed();
	}

	<I extends Exception> void around JP() throws I{
		C.counter++;		
		try{
			proceed();
		}
		catch(IOException i){}
	}
}
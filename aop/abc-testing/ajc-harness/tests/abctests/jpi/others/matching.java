import java.io.IOException;

<R extends IOException>jpi void JP() throws R;

public class C{

	exhibits void JP(): execution(* bar(..));
    
    public static void bar() throws Exception{}
    public static void main(String[] args){
    	try{
    		bar();
    	}
    	catch(Exception e){}
    }

}

aspect A{

    <L extends Exception> void around JP() throws L{ 
    	System.out.println("pasé");
    	try{
    		proceed();
    	}
    	catch(Exception e){}
    }
}
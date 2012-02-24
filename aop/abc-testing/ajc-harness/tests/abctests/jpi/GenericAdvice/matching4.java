/*
 * We use DummyAdviceDecl to check exhibits decl against join points.
 * Advice decl will able to catch Exception, IOException from base code, because
 * jpi type decl defines Exception.
 */

import java.io.IOException;

<R extends Exception>jpi void JP() throws R;

public class C{

	exhibits void JP(): execution(* bar(..)); 
    
    public static void bar() throws IOException, Exception{}
    public static void main(String[] args){
    	try{
    		bar();
    	}
    	catch(Exception e){}
    }
}

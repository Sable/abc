/*
 * We use DummyAdviceDecl to check exhibits decl against join points.
 * Advice decl will not able to catch Exception from base code, because
 * jpi type decl only defines IOException.  Also, Advice decl defines in its 
 * throws list a type of IOException.
 */

import java.io.IOException;

<R extends IOException>jpi void JP() throws R;

public class C{

	exhibits void JP(): execution(* bar(..)); 
    
    public static void bar() throws Exception{} //error
    public static void main(String[] args){
    	try{
    		bar();
    	}
    	catch(Exception e){}
    }
}

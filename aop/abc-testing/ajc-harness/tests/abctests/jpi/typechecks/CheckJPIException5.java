/**
 * The jpi type exception is a subtype of the advice type exception.
 * In this case, there is no problem with proceed().
 */

import java.io.*;

jpi void JP() throws IOException;

public class CheckJPIException5{

    exhibits void JP():
        execution(* foo(..)); 

    void foo() throws IOException{
        throw new IOException();    
    }

    public static void main(String[] args){
        CheckJPIException5 ce = CheckJPIException5();
        try{
            ce.foo();
        }catch(IOException e){}
    }
}

aspect A{

    void around JP() throws Exception{ //error: Exception not allowed by JP
        proceed(); //ok
    }
}

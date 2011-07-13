/**
 * The call of proceed() raises an Exception, but the advice header
 * doesn't catch this type of exceptions.
 */

import java.io.*;

jpi void JP() throws Exception;

public class CheckJPIException3{

    exhibits void JP():
        execution(* foo(..)); 

    void foo() throws Exception{
        throw new Exception(); 
        //throw new IOException(); //it is an error too.        
    }

    public static void main(String[] args){
        CheckJPIException3 ce = CheckJPIException3();
        try{
            ce.foo();
        }catch(Exception e){}
    }
}

aspect A{

    void around JP() throws IOException{
        proceed(); //can't raise Exception.
    }
}

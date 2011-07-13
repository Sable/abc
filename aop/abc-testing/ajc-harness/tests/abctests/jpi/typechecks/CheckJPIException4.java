/**
 * The call of proceed() raises an Exception, but the advice
 * body handles this exception.
 */

import java.io.*;

jpi void JP() throws Exception;

public class CheckJPIException4{

    exhibits void JP():
        execution(* foo(..)); 

    void foo() throws Exception{
        throw new Exception();    
    }

    public static void main(String[] args){
        CheckJPIException4 ce = CheckJPIException4();
        try{
            ce.foo();
        }catch(Exception e){}
    }
}

aspect A{

    void around JP() throws IOException{
        try{
            proceed();
        }catch(Exception e){
            throw new IOException();
        }
    }
}

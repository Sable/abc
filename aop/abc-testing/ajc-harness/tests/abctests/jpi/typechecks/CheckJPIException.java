/**
 * jpi doesn't indicate an exception, but the advice related with it yes. 
 * Through an static analysis is it possible say that the program is safe. Is it possible?
 */

import java.io.*;

jpi void JP();

public class CheckJPIException{

    exhibits void JP():
        execution(* foo(..)); 

    void foo(){}

    public static void main(String[] args){
        CheckJPIException ce = CheckJPIException();
        ce.foo();
    }
}

aspect A{

    void around JP() throws IOException{ // error: IOException not allowed for JP
        proceed();
    }
}

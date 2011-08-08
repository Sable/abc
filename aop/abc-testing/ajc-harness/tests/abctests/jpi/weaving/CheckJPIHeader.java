/**
 * This test checks the jpi signatures with throws clause.
 */

import java.lang.Exception;
import java.io.IOException;

jpi void JP();
jpi void JP1() throws IOException; 
jpi void JP2() throws Exception; 
jpi void JP3() throws IOException; 

public class CheckJPIHeader{

    exhibits void JP() :  //error.
        execution(* foo(..));

    exhibits void JP1() : //ok, IOException is the same type of base code.
        execution(* foo(..));

    exhibits void JP2() : //error, base code doesn't handle this type of exception.
        execution(* foo(..));

    exhibits void JP3() : //error because advice may not be able to handle general Exceptions when calling proceed()
        execution(* bar(..));

    void foo() throws IOException{}

    void bar() throws Exception{}
}

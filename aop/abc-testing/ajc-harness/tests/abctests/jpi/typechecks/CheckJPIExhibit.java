/**
 * This test checks the jpi signatures with throws clause.
 */

import java.lang.*;

jpi Integer JP(char x, Object i);
jpi Integer JP2(char x, Object i);
jpi Integer JP3(char x, Object i);
jpi Integer JP4(char x, Object i);
jpi Integer JP5(char x, Object i);

public class CheckJPIExhibit{

    exhibits void JP1() :  //error, JP1 doesn't exist.
        execution(* foo(..));

    exhibits void JP(char l, Object k) : //wrong, return type must be the same.
        execution(* foo(..)) && args(l,k);

    exhibits Integer JP3(char x, Integer i) : //wrong, arguments must have the same type. 
        execution(* foo(..)) && args(x,i);

    exhibits Integer JP4(char x, Object p) : //ok
        execution(* bar(..)) && args(x,p);

    exhibits Integer JP5(char x) : //wrong, must be the same quantity of args.
        execution(* bar(..)) && args(x);

}

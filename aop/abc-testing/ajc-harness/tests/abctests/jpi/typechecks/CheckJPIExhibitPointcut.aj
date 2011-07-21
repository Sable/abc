/**
 * This test checks the jpi signatures with throws clause.
 */

import java.lang.*;

jpi Integer JP(char x, Object i);
jpi Integer JP2(char x, Object i);

class CheckJPIExhibit{

    exhibits Integer JP(char z, Object f) :
        execution(* foo(char,Object)) && args(z,f);

    exhibits void JP2(char l, Object k) :
        execution(* foo(..)) && args(l);

}


public aspect a{
	
	pointcut Hola(int i): execution(* foo(..)) && args(i);
	
}
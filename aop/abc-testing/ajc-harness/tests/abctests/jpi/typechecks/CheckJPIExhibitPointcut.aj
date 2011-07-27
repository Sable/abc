/**
 * This test checks the jpi signatures with throws clause.
 */

import java.lang.*;

jpi Integer JP(char x, Object i);
jpi Integer JP1(char x, Object i);
jpi Integer JP2(char x, Object i);

class CheckJPIExhibit{

    exhibits Integer JP(char z, Object f) :
        execution(* foo(char,Object)) && args(z) && this(f);

    exhibits Integer JP1(char z, Object f) :
        execution(* foo(char,Object)) && args(z,f) && this(l);  //wrong, l is not found.  
    
    exhibits void JP2(char l, Object k) : //wrong, return type and missed the binding for k.
        execution(* foo(..)) && args(l);
}


public aspect a{
	
	pointcut Hola(int i): execution(* foo(..)) && args(i);
	
}
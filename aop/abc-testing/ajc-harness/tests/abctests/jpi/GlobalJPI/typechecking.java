/**
 * This test checks the jpi signatures with throws clause.
 */

import java.lang.*;

global jpi Integer JP(char z, Object f) : 
	execution(* foo(char,Object)) && args(z) && this(f);

global jpi Integer JP1(char z, Object f) : 
	execution(* foo(char,Object)) && args(z,f) && this(l);  //wrong, l isn't defined.  

global jpi Integer JP2(char l, Object k) : //wrong, k was not bound.
	execution(* foo(..)) && args(l); 

<R> global jpi R JPG():
	execution(R foo(char,Object));

<R> global jpi R JPG(R a):
	execution(R foo(char,Object)) && args(a);

<R> global jpi R JPG(R a, R f):
	execution(R foo(char,Object)) && args(a) && this(f);

<L extends Integer> global jpi L JPG(L t): //wrong, t was not bound.
	execution(R foo(char,Object)); 

<L extends Integer> global jpi L JPG(L a, L f):
	execution(R foo(char,Object)) && args(a,f) && this(k); //wrong, k isn't defined.

package klass;

import interfaces.JPI;

public class JPIDeclared{

    exhibits void JPI() : //ok
        execution(* foo(..));

    exhibits void interfaces.JPI() : //ok
    	execution(* foo(..));

    exhibits void JP2() : //error, jpi JP2 doesn't exist
    	execution(* foo(..));
    
    exhibits void interfaces.JP2() : //error, jpi interfaces.JP2 doesn't exist
    	execution(* foo(..));
    
    void foo(){}

    public static void main(String[] args){
        new JPIDeclared().foo();
    }
}
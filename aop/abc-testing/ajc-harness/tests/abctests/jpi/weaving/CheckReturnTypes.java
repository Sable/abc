/*
 * This test case is used to verify that the typecheck in the weave time works properly
 */

import java.io.*;
import java.lang.*;

jpi Integer JP(int a);
jpi String JP2(int b);

public class CheckJPIDeclared{

    exhibits Integer JP(int i): //error, must be Integer not Number.
        execution(* foo(..)) && args(i);

    exhibits String JP2(int x): //error, must be Integer not Object.
        execution(* bar(..)) && args(x); 


    String foo(int i){ return null;} //but are marked here :-(

    Integer bar(int n) { return null;} //but are marked here :-(
}

aspect A{

    Integer around JP(int i){
        return proceed(i); 
    }

    String around JP2(int x){ 
        return proceed(x); 
    }
}

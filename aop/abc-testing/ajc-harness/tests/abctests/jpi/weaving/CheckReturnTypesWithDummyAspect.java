/*
 * This test case is used to verify that the typecheck in the weave time works properly
 * In this test case, we use the $dummyAspect$ with its DummyAdvices to do the check.
 */


import java.io.*;
import java.lang.*;

jpi Integer JP(int a);
jpi String JP2(int b);
jpi boolean JP3(String z);

public class CheckReturnTypesWithDummyAspect{

    exhibits Integer JP(int i): //error, must be Integer not String.
        execution(* foo(..)) && args(i);

    exhibits String JP2(int x): //error, must be String not Integer.
        execution(* bar(..)) && args(x); 

    exhibits boolean JP3(String m): //error, must be boolean not int.
        execution(* zar(..)) && args(m); 

    String foo(int i){ return null;} //but are marked here :-(

    Integer bar(int n) { return null;} //but are marked here :-(
    
    int zar(String l) {return 1;} //but are marked here :-(
}
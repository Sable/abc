import java.io.*;
import java.lang.*;

jpi Integer JP(int a);
jpi String JP2(int b);

public class CheckReturnTypesWithDummyAspect{

    exhibits Integer JP(int i): //error, must be Integer not Number.
        execution(* foo(..)) && args(i);

    exhibits String JP2(int x): //error, must be Integer not Object.
        execution(* bar(..)) && args(x); 


    String foo(int i){ return null;} //but are marked here :-(

    Integer bar(int n) { return null;} //but are marked here :-(
}
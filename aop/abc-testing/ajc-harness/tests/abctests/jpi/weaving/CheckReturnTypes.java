import java.io.*;
import java.lang.*;

jpi Integer JP(int a);
jpi Object JP2(int b);

public class CheckJPIDeclared{

    exhibits Number JP(int i): //error, must be Integer not Number.
        execution(* foo(..)) && args(i);

    exhibits Object JP2(int x): //error, must be Integer not Object.
        execution(* bar(..)) && args(x); 


    Number foo(Integer i){ return i;}

    Integer bar(Number n) { return 5;}
}

aspect A{

    Integer around JP(int i){
        return proceed(i); 
    }

    int around JP2(int x){ //error, must be Object not int
        return proceed(x); 
    }
}

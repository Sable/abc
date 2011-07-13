import java.io.*;

jpi Integer JP(int a);
jpi Object JP2(int b);

public class CheckJPIDeclared{

    exhibits void JP(int i): //error, must be Integer not Number.
        execution(* foo(..)) && args(i);

    exhibits void JP2(int x): //error, must be Integer not Object.
        execution(* bar(..)) && args(x); 


    Number foo(Integer i){}

    Integer bar(Number n) {}

    public static void main(String[] args){
        CheckJPIException ce = CheckJPIException();
        Number x = ce.foo(5);
        Integer n = ce.bar(5);
    }
}

aspect A{

    Integer around JP(int i){
        return proceed(i); 
    }

    Object around JP2(int x){
        return proceed(x); 
    }
}

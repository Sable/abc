import java.io.*;

jpi void JP(Number a);
jpi void JP2(Number b);

public class CheckJPIDeclared{

    exhibits void JP(Number i): //error, expected Integer type
        execution(* foo(..)) && args(i);

    exhibits void JP2(Number x):
        execution(* bar(..)) && args(x); 


    void foo(Integer i){}

    void bar(Number n) {}

    public static void main(String[] args){
        CheckJPIException ce = CheckJPIException();
        Integer x = 5;
        Number n = 5;
        ce.foo(x);
        ce.bar(n);
    }
}

aspect A{

    void around JP(Number i){
        proceed(i);
    }

    void around JP2(Number x){
        proceed(x); //ok.
    }
}

public class Test  {
   double foo(double x) {
      return x;
   }
}

aspect Aspect {

pointcut foo(int b): args(b);

before(Object i) : foo(i) 
   {
   }
}

public class Test {

   int x=0;

   public int fact(int n) {
     x++;
     if(n>1) return n*fact(n-1); else return 1;
   }

   public static void main(String[] args) {
     System.out.println(new Test2().fact(5));
   }
}

class Test2 extends Test { }

aspect Aspect percflowbelow(call(int fact(int)) && this(Test2)) {
   private static boolean debug = true;
   int y;
   before (int x) : set(int x) && args(x) && if(debug) {
       y=x;
   }
   after () : execution(int fact(int)) {
       System.out.println(y);
   }
}


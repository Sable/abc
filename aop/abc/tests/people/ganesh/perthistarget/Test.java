public class Test {

   public int x=0;
   public void foo() {
     x=1;
   }

   public static void main(String[] args) {
     new Test().foo();
     new Test().foo();
     new Test().foo();
   }
}

aspect Aspect perthis(within(*)) {
   private static boolean debug = true;
   before () : set(int x) && if(debug) {
   }
}

aspect Aspect2 pertarget(within(*)) {
   private static boolean debug = true;
   before () : set(int x) && if(debug) {
   }
}

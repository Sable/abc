public class Test {
   void bar(int x,int y,int z,int w,int t) {

   }

   public static void main(String[] args) {
      new Test().bar(1,2,3,4,5);
   }

}

aspect Aspect {
   after(Object x) : args(x,..) && execution(void bar(..)) {      
       System.out.println(thisJoinPoint);
   }
}

/*
aspect Aspect2 {
   declare precedence: Aspect, Aspect2;
   void around(Object x,Object y): this(x) && target(y) && execution(void bar(..)) {
      proceed(y,x);
   }
}
*/

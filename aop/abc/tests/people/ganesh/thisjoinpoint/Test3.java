public class Test3 {
   void bar(int x,int y,int z,int w,int t) {

   }

   public static void main(String[] args) {
      new Test3().bar(1,2,3,4,5);
   }

}

aspect Test3Aspect {
   after(Object x) : args(x,..) && execution(void bar(..)) {      
       System.out.println(thisJoinPoint.getKind());
   }
}

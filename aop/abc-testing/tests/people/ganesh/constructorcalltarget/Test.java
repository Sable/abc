public class Test {
   public static void main(String[] args) {
      Test x=new Test();
   }
}

aspect Aspect {
   before() : call(*.new(..)) {
   }

   after() : call(*.new(..)) {
   }
}

public class Test2 {
   static {
      Test2 x = new Test2(splat(3));
   }

   void foo() {
      Test2 x = new Test2(splat(3));
   }

   static int splat(int i) {
     return i+1;
   }

   void bar(int i) {
      Test2 x = i>3 ? new Test2(i) : null;
   }

   Test2(int v) {
   }


}

aspect A {
    before() : call(*.new(..)) { }
}

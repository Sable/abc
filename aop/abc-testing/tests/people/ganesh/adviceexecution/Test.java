public class Test {


}

aspect Aspect {
   before(): call(* *.*(..)) && !adviceexecution() {
      System.out.println("foo");
   }
}

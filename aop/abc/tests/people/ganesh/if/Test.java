public class Test {
   public static void main(String[] args) {

   }

}

aspect Aspect {
   before() : execution(void Test.main(..)) && !!!if(true) && !!if(true) || !if(true) && if(true) { }
}

public aspect Aspect10 {

int countpointcuts = 0;

static boolean debug = true;

before() : call(int Test.foo(..))
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

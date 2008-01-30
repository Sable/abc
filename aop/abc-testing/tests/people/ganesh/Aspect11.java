public aspect Aspect11 {

int countpointcuts = 0;

static boolean debug = true;

before() : call(int Test.foo(..,int,..,int,..))
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

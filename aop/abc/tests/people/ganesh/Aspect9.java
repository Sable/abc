public aspect Aspect9 {

int countpointcuts = 0;

static boolean debug = true;

before() : initialization(*..new(..))
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

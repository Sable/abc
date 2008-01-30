public aspect Aspect8 {

int countpointcuts = 0;

static boolean debug = true;

before() : staticinitialization(*)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

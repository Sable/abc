public aspect Aspect12 {

int countpointcuts = 0;

static boolean debug = true;

before() : get(!static * *.*) && this(Baz)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

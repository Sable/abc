public aspect Aspect {

int countpointcuts = 0;

static boolean debug = true;

pointcut allgets (): this(a);

before() : allgets() 
   {  System.out.println("I am in a pointcut");
      countpointcuts++;
   }
}

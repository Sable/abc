public aspect Aspect5 {

int countpointcuts = 0;

static boolean debug = true;

before(Object x) : this(x)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

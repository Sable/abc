public aspect Aspect4 {

int countpointcuts = 0;

static boolean debug = true;

before() : set(* *.*) && this(Object)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

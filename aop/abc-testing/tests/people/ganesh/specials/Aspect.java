public aspect Aspect {

int countpointcuts = 0;

static boolean debug = true;

before (): execution(* *(..)) || get(* *.*)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

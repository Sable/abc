public aspect Aspect3 {

int countpointcuts = 0;

static boolean debug = true;

before() : set(* *.*) && set(* *.*)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

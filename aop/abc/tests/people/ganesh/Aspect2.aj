public aspect Aspect {

int countpointcuts = 0;

static boolean debug = true;

pointcut allgets () : set(* *.*);

before() : allgets() 
   {  System.out.println("I am in a pointcut " + 
         thisJoinPoint + thisJoinPointStaticPart);
      countpointcuts++;
   }
}

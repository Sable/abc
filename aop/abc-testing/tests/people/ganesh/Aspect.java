public aspect Aspect {

int countpointcuts = 0;

static boolean debug = true;

// pointcut allgets (Object a) : !within(Aspect*) && target(a) && set(* *.*);

pointcut allgets (Object a) : set(* *.*);

before(Object a) : allgets(a) 
   {  System.out.println("I am in a pointcut " + 
         thisJoinPoint + thisJoinPointStaticPart +  a);
      countpointcuts++;
   }
}

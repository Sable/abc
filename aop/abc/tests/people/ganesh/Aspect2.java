public aspect Aspect2 {

int countpointcuts = 0;

static boolean debug = true;

before() : !within(Aspect*) // set(* *.*)
   {  System.out.println("I am in a pointcut "+thisJoinPointStaticPart+thisEnclosingJoinPointStaticPart);
      countpointcuts++;
   }
}

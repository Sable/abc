public aspect Aspect {

int countpointcuts = 0;

static boolean debug = true;

after (Baz x) throwing: this(x)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

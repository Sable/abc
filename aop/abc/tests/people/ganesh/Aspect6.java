public aspect Aspect6 {

int countpointcuts = 0;

static boolean debug = true;

after(Object x) returning (int y) : this(x)
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

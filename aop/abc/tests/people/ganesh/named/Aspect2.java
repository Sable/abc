public aspect Aspect2 {

int countpointcuts = 0;

static boolean debug = true;

pointcut allgets(Blat a,int b): this(a) && args(b);

before(Object i) : allgets(Baz,i) 
   {  System.out.println("I am in a pointcut");
      countpointcuts++;
   }


}

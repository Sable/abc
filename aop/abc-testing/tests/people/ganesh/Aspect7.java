public aspect Aspect7 {

int countpointcuts = 0;

static boolean debug = true;

before() : call(*..new(..))
   {  System.out.println("I am in a pointcut ");
      countpointcuts++;
   }
}

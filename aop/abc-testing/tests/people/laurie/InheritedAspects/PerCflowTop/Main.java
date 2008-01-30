public class Main {

  public int x;

  public int foo (int x) { return(x+1); }

  public static Main v = new Main(13);

  public Main(int x) { this.x = x; }

  public static void main (String args[])
   { Main a = new Main(14);
     int x = v.foo(40);
     int y = a.foo(40);
     a.x = 5;
     int z = a.x + 1;
   }

  pointcut all() : !(within(Aspect*)) ;

  pointcut allfoo() : call(* *.foo(..)) ;
}

abstract aspect Aspect_A_percflow percflow(Main.all()) {
  public static int k = 13;
  before() : Main.all() && cflow(Main.allfoo()) && if(k%2 == 1)
     { System.out.println("Aspect_A_singleton " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_none extends Aspect_A_percflow {
  before() : Main.all() 
     { System.out.println("Aspect_B_none " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_singleton extends Aspect_A_percflow issingleton() {
  before() : Main.all() 
     { System.out.println("Aspect_B_singleton " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_pertarget extends Aspect_A_percflow pertarget(Main.all()) {
  before() : Main.all()
     { System.out.println("Aspect_B_pertarget " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_percflow extends Aspect_A_percflow percflow(Main.all()) {
  before() : Main.all()
     { System.out.println("Aspect_B_percflow " + thisJoinPointStaticPart);
     }
}


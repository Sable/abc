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
}

abstract aspect Aspect_A_singleton issingleton() {
  before() : Main.all()
     { System.out.println("Aspect_A_singleton " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_none extends Aspect_A_singleton {
  before() : Main.all() 
     { System.out.println("Aspect_B_singleton " + thisJoinPointStaticPart);
     }
} 


aspect Aspect_B_singleton extends Aspect_A_singleton issingleton() {
  before() : Main.all() 
     { System.out.println("Aspect_B_singleton " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_pertarget extends Aspect_A_singleton pertarget(Main.all()) {
  before() : Main.all()
     { System.out.println("Aspect_B_pertarget " + thisJoinPointStaticPart);
     }
}

aspect Aspect_B_percflow extends Aspect_A_singleton percflow(Main.all()) {
  before() : Main.all()
     { System.out.println("Aspect_B_percflow " + thisJoinPointStaticPart);
     }
}


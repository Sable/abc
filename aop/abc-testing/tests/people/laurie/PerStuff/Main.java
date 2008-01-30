public class Main {
  public static void main (String args[])
     { A a = new A();
       a.f = 3;
       a.toString();
     }
}

class A {
   int f;
   public String toString() { return(" f is " + f); }
}


aspect One {
  declare precedence: Zero,One,Two,Three;

  after() : get(* *.f) { System.out.println("After One (a)"); }
  before() : get(* *.f) { System.out.println("Before One (a)"); }

  Object around() : get(* *.f) 
    { System.out.println("Around One - before proceed (a)");
      Object r = proceed();
      System.out.println("Around One - after proceed (a)");
      return(r);
    }

  Object around() : get(* *.f) 
    { System.out.println("Around One - before proceed (b)");
      Object r = proceed();
      System.out.println("Around One - after proceed (b)");
      return(r);
    }

  before() : get(* *.f) { System.out.println("Before One (b)"); }
  after() : get(* *.f) { System.out.println("After One (b)"); }
  after() : get(* *.f) { System.out.println("After One (c)"); }
}

aspect Three {
  before() : get(* *.f) { System.out.println("Before Three (a)"); }
  before() : get(* *.f) { System.out.println("Before Three (b)"); }
}

aspect Two {
  before() : get(* *.f) { System.out.println("Before Two (a)"); }
  before() : get(* *.f) { System.out.println("Before Two (b)"); }
}

aspect Zero {
  before() : get(* *.f) { System.out.println("Before Zero (a)"); }
  before() : get(* *.f) { System.out.println("Before Zero (b)"); }
}


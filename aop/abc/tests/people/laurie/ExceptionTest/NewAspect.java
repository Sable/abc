public aspect NewAspect {
  before () : set(* *.*) { System.out.println("before get");  }
  after () returning : set(* *.*) { System.out.println("after get"); }

  before() : execution(* *..*(..)) { System.out.println("before execution 1"); }
  before() : execution(* *..*(..)) { System.out.println("after execution 2"); }

  after() returning : execution(* *..*(..)) 
     { System.out.println("before execution 1"); }
  after() returning: execution(* *..*(..)) 
     { System.out.println("after execution 2"); }
}

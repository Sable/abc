public aspect NewAspect {

  static int aspectlevel = 0;

  static void message(String s) 
    { for (int i=0; i < aspectlevel; i++) System.out.print("===+");
      System.out.println(s);
    }
  before () : set(* *.*) && !within(NewAspect) 
    { message("before set"); aspectlevel++; }
  after () returning : set(* *.*) && !within(NewAspect) 
    { aspectlevel--; message("after set"); }

  before () : get(* *.*) && !within(NewAspect) 
    { message("before get");  aspectlevel++; }
  after () returning : get(* *.*) && !within(NewAspect) 
    { aspectlevel--; message("after get"); }

  before() : execution(* *..*(..)) && !within(NewAspect)
    { message("before execution 1"); aspectlevel++; }
  before() : execution(* *..*(..)) && !within(NewAspect) 
    { message("before execution 2"); aspectlevel++; }

  after() returning : execution(* *..*(..)) && !within(NewAspect) 
     { aspectlevel--; message("after execution 1"); }
  after() returning: execution(* *..*(..)) && !within(NewAspect) 
     { aspectlevel--; message("after execution 2"); }

  before() : initialization(*..new(..)) && !within(NewAspect)
     { message("before initalization new"); aspectlevel++; }
  after() returning: initialization(*..new(..)) && !within(NewAspect) 
     { aspectlevel--; message("after initialization new"); }

  before() : preinitialization(*..new(..)) && !within(NewAspect)
     { message("before preinitialization new"); aspectlevel++; }
  after() returning: preinitialization(*..new(..)) && !within(NewAspect) 
     { aspectlevel--; message("after preinitialization new"); }

  before() : execution(*..new(..)) && !within(NewAspect)
     { message("before execution new"); aspectlevel++; }
  after() returning: execution(*..new(..)) && !within(NewAspect) 
     { aspectlevel--; message("after execution new"); }

  before() : staticinitialization(NewTest)
     { message("before static initialization"); aspectlevel++; }
  after() returning : staticinitialization(NewTest)
     { aspectlevel--; message("after static initialization");  }



}

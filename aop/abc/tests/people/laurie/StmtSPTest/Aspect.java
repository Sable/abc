public aspect Aspect {

  static int aspectlevel = 0;

  static void message(String s) 
    { for (int i=0; i < aspectlevel; i++) System.out.print("===+");
      System.out.println(s);
    }

  // set and get
  before () : set(* *.*) && !within(Aspect) 
    { message("before set"); aspectlevel++; }
  after () returning : set(* *.*) && !within(Aspect) 
    { aspectlevel--; message("after set"); }

  before () : get(* *.*) && !within(Aspect) 
    { message("before get");  aspectlevel++; }
  after () returning  : get(* *.*) && !within(Aspect) 
    { aspectlevel--; message("after get"); }

  // calls to methods
  before () : call(* *..*(..)) && !within(Aspect)
    { message("before method call"); aspectlevel++; }
  after () returning : call(* *..*(..)) && !within(Aspect)
    { aspectlevel--; message("after method call");  }

  // calls to constructors
  before () : call( *..new(..)) && !within(Aspect)
    { message("before constructor call"); aspectlevel++; }
  after () returning : call(*..new(..)) && !within(Aspect)
    { aspectlevel--; message("after constructor call");  }
  
  // handler
  before () : handler(*) && !within(Aspect)
    { message("before handler"); }

}

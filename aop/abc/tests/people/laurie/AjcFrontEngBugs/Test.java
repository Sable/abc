public class Test {
  public static int if1(int x) // 1st before advice should match here
   { return(x+1);
   }

  public static int while1(int x) // 2nd before advice should match here
   { return(x+1);
   }

  public static void main(String args[])
    { System.out.println(if1(1));
      System.out.println(while1(1));
    }
}

aspect Aspect {
  before() : call(int if*(int)) && within(Test) // this causes a parse error in ajc
    { System.out.println("before method starting with if");
    }

  before() : call(int while*(int)) && within(Test)  // this is ok
    { System.out.println("before method starting with while");
    }
}

public class Bug {
  // no static initializer in Bug
}

class BugExt extends Bug {

  static int forceclinit = 13;

  public static void main(String args[])
   { BugExt b = new BugExt();
     System.out.println("Exiting main");
   } 
}

aspect StaticInit{
  // this should never match
  before() : staticinitialization(Bug)  
    { System.out.println("I am in the staticinitialization of Bug");
    }
}

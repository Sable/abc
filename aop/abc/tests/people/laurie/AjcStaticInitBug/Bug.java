public class Bug {
  // no static initializer in Bug
  // but the ajc compiler seems to create an empty one,  and then the advice
  // in the StaticInit aspect below matches this inserted empty clinit().
  // Is that the desired/expected behaviour?   I would have thought that
  // a clinit() method only appears in a class when there things to be put
  // in it?   Certainly javac does not create one.
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

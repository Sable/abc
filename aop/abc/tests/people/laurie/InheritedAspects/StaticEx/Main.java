/* static aspects have to be nested */
/* nested aspects have to be static */

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

  pointcut all() : !within(Main.AspectInner) && 
                   !within(Main.AspectInner.AspectInnerInner) && 
                   !within(AspectOuter) && 
                   !within(AspectOuter.AspectInner2) &&
                   !within(I.AspectInner3);

  static aspect AspectInner {  
    int x;
    before () : Main.all() {
      System.out.println("ONE: static aspect inside class  " + 
          thisJoinPointStaticPart) ; 
    }
    static aspect AspectInnerInner { 
      int x;
      before () : Main.all() {
        System.out.println("TWO: static aspect inside aspect inside class  " + 
          thisJoinPointStaticPart) ; 
      }
    }
  }
}

aspect AspectOuter percflow(Main.all()) {

  declare precedence: I.AspectInner3, // FOUR
                      AspectOuter.AspectInner2,  // THREE
                      Main.AspectInner.AspectInnerInner, // TWO
                      Main.AspectInner; // ONE

  static aspect AspectInner2 {  
    before () : Main.all() {
      System.out.println("THREE: static aspect inside aspect " + 
          thisJoinPointStaticPart) ; 
    }
  }
}

interface I {
  static aspect AspectInner3 { 
    before () : Main.all() { // Main.all() doesn't work
      System.out.println("FOUR: static aspect inside interface " + 
          thisJoinPointStaticPart) ; 
    }
  }

  static class ClassInsideInter { }
}

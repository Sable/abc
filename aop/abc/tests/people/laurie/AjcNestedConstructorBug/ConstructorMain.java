public class ConstructorMain {
 // Note that in the case of this() calls in a constructor, the
 // treatment of preinitialization and initialization join points are
 // not correct, and are sensitive to the order in which constructors
  // are given in the class file.   
  //
  // Below we see everything is ok for preinit and init join points for
  // constructors in class B,  but are not correct for class C.  The only
  // difference is in the order in which the constructors are given.  The
  // inlining strategy for handling this() calls must be broken in ajc.
  
  public static void main(String args[])
    { int k = 100; 
      // These are ok, note order constructors given in class
      System.out.println("----------------------"); 
      B b3 = new B(3+k, 4+k);
      System.out.println("----------------------"); 
      B b2 = new B(2 + k);
      System.out.println("----------------------"); 
      B b1 = new B();

      // First two are ok, but last one not ok 
      System.out.println("----------------------"); 
      C c3 = new C(3+k, 4+k);
      System.out.println("----------------------"); 
      C c2 = new C(2 + k);
      System.out.println("----------------------"); 
      C c1 = new C();

      System.out.println("----------------------"); 
    }
}

class A {
  int x = 4;
  A (int x) { this.x = x; }
} 

class B extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  B (int x, int y) { super(x+y); this.y = x+y; }

  B (int x) { this(x+l, x+l); this.y = x+l; } 

  B () { this(k+j); this.y = l; }


}


class C extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  C () { this(k+j); this.y = l; }

  C (int x) { this(x+l, x+l); this.y = x+l; } 

  C (int x, int y) { super(x+y); this.y = x+y; }

}

aspect ConstructorAspects {

  static private int aspectnesting = 0;

  static void message(String s)
    { for (int i=0; i<aspectnesting; i++) System.out.print("---+");
      System.out.println(s);
    }


  // call of all constructors 
  pointcut allconstrcalls() :  call(*..new(..)) &&
           !within(ConstructorAspects) && !call(java.lang..new(..));

  // execution of all constructors
  pointcut allconstrexecutions() : execution(*..new(..)) && 
           !within(ConstructorAspects);

  // intialization of all constructors
  pointcut allconstrinitializations() : initialization(*..new(..)) &&
           !within(ConstructorAspects);

  // preinitialization of all constructors
  pointcut allconstrpreinitializations() : preinitialization(*..new(..)) &&
          !within(ConstructorAspects);

  // before advice
  before () : !within(ConstructorAspects) {
	      message(
		  "BEFORE: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      aspectnesting++;
	      }

  // after advice
  after () returning : !within(ConstructorAspects) {
              aspectnesting--;
	      message(
		  "AFTER: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      }

}



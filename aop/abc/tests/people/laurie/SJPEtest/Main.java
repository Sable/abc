public class Main {
  
  public static void main(String args[])
    { int k = 100; 
      B b3 = new B(
	   3+k, 
	   4+k);
      B b2 = new B(
	    2 + k);
      B b1 = new B();
      b1.foo(3);
      try {
	int x = b2.foo(4) / 0;
      }
      catch (Exception e)
      { System.out.println("                         " +    e);
      }
    }
}

class A {

  int x = 4;

  A (int x) 
    { this.x = x; }
} 

class B extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  int foo(int i) 
    { y = y + i;  
      return(i+1); 
    }

  B (int x, int y) 
    { super(
	x
	+
	y); 
      this.y = 
	x
	+
	y; }

  B (int x) 
    { this(
	x+l, 
	x+l); 
      this.y = x+l; } 

  B () 
    { this(
	k+
	j); 
      this.y = l; }

}


aspect Aspects {

  static private int aspectnesting = 0;

  static void message(String s)
    { for (int i=0; i<aspectnesting; i++) System.out.print("---+");
      System.out.println(s);
    }

  // before advice
  before () : !within(Aspects) 
   { message(
	  "BEFORE: " +  
          thisJoinPointStaticPart.getKind() + " at " +
		      thisJoinPointStaticPart.getSourceLocation() );
     message(
	  "  enclosed by " + 
	  thisEnclosingJoinPointStaticPart.getKind() + " at "
	  +  thisEnclosingJoinPointStaticPart.getSourceLocation());
          if (!thisJoinPointStaticPart.getKind().equals("exception-handler")) 

        	aspectnesting++;
	      }

  // after advice
  after () returning : !within(Aspects)  && !handler(*)
            {  aspectnesting--;
	      message(
		  "AFTER: " +  
		      thisJoinPointStaticPart.getKind() + " at " +
		      thisJoinPointStaticPart.getSourceLocation() );
	      message(
		  "  enclosed by " + 
		  thisEnclosingJoinPointStaticPart.getKind() + " at "
		  +  thisEnclosingJoinPointStaticPart.getSourceLocation());


	     }
}

public class NewTest {
  int a = 2;

  public NewTest() { }

  public NewTest(int a) { this.a = a; }

}

class NewTestExt extends NewTest {

  int b = 3;
  int c = 4;
  static int forceclinit = 13;

  // a call to super with exp 
  public NewTestExt()
    {  super(new Integer(0).intValue()); 
       this.b = 0;
       this.c = 0;
    }

  // creating objects in args to this, make sure correct <init> is found
  public NewTestExt(int i)
    { super(new Integer(i).intValue());
      b = i;
      c = i;
    }

  // creating NewTest args in this, make sure correct <init> is found
  public NewTestExt(int b, int c)
    { super(new NewTestExt(2).a + b);
      this.b = b;
      this.c = c;
    }

  // creating NewTest with multiple returns
  public NewTestExt(int a, int b, int c)
    {  if ( a == 0 )
         { this.a = 0; return; }
        else if (b == 0)
	 { this.b = 1; return; }
	else if (c == 0)
	 { this.c = 2; return; }
        this.a = a;
        this.b = b;
        this.c = c;
     }	
      
  // body ending in a goto
  public int c(int x)
    { L1: { if (x == 0) return(x); 
	    x--;
	    break L1;
	  }
      return(0);
    }

  // bunch of returns,  returning a constant
  public int d(int x)
    { if (x > 0)
        return(1);
      if (x == 0)
	return(0);
      return(-1);
    }

   // branch to final return
  public void e(int x)
    { if (x != 0)
        { x = x * x;
	}
     }

  // whole body is a try-catch
  public void f(int x)
    { try
        { a = x/0;
	}
      catch (Exception e)
        { a = 1;
        }
    }

  // branch to first instruction in body
  public int g(int x)
    { do
        { x--;
	}
      while (x > 0);
      return(x);
    }

  // only one return
  public int h (int x)
    { return(x);
    }


  public static void main(String args[])
   { int x = 4;
     int y = 5; 
     
     System.out.println("--- NewTestExt()");
     NewTestExt t = new NewTestExt();
     System.out.println("--- NewTestExt(int)");
     NewTestExt t2 = new NewTestExt(1);
     System.out.println("--- NewTestExt(int,int)");
     NewTestExt t3 = new NewTestExt(2,3);
     System.out.println("--- NewTestExt(int,int,int)");
     NewTestExt t4 = new NewTestExt(4,5,6);
     System.out.println("--- calling c");
     t.c(0);
     System.out.println("--- calling d");
     t.d(1);
     System.out.println("--- calling e");
     t.e(2);
     System.out.println("--- calling f");
     t.f(3);
     System.out.println("--- calling g");
     t.g(4);
     System.out.println("--- calling h");
     t.h(5);
   }
}



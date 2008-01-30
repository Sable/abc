public class Test {
  int a = 2;

  public Test() { }

  public Test(int a) { this.a = a; }

}

class MyException extends RuntimeException {
}

class TestExt extends Test {

  int b = 3;
  int c = 4;
  static int forceclinit = 13;

  // a call to super with exp 
  public TestExt()
    {  super(new Integer(0).intValue()); 
       this.b = 0;
       this.c = 0;
    }

  // creating objects in args to this, make sure correct <init> is found
  public TestExt(int i)
    { super(new Integer(i).intValue());
      b = i;
      c = i;
    }

  // creating Test args in this, make sure correct <init> is found
  public TestExt(int b, int c)
    { super(new TestExt(2).a + b);
      this.b = b;
      this.c = c;
    }

  // creating Test with multiple returns
  public TestExt(int a, int b, int c)
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

  // whole body is a try-catch, execution will go to the catch
  public void f(int x)
    { try
        { a = x/0;
	}
      catch (Exception e)
        { a = 1;
	}
    }

  static int outsidex;

  // a field read inside a try catch,  before and after advice
  // should go inside that region
  // whole body is a try-catch, execution will go to the catch
  public void f2(int x)
    { try
        { outsidex = x;
	}
      catch (Exception e)
        { outsidex = 0; 
	}
    }


  // branch to first instruction in body
  public int g(int x)
    { do
        { outsidex = x; // should have a goto to this field set 
	  x--;
	}
      while (x > 0);
      return(x);
    }

  // branch to first instruction in body
  public int g2(int x)
    { do
        { Object o = new Object(); // should have a goto to this new call 
	  x--;
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
     
     System.out.println("--- TestExt()");
     TestExt t = new TestExt();
     System.out.println("--- TestExt(int)");
     TestExt t2 = new TestExt(1);
     System.out.println("--- TestExt(int,int)");
     TestExt t3 = new TestExt(2,3);
     System.out.println("--- TestExt(int,int,int)");
     TestExt t4 = new TestExt(4,5,6);
     System.out.println("--- calling c");
     t.c(0);
     System.out.println("--- calling d");
     t.d(1);
     System.out.println("--- calling e");
     t.e(2);
     System.out.println("--- calling f");
     t.f(3);
     System.out.println("--- calling f2");
     t.f2(3);
     System.out.println("--- calling g");
     t.g(4);
     System.out.println("--- calling g2");
     t.g2(4);
     System.out.println("--- calling h");
     t.h(5);
   }
}



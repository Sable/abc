public class NewTest {
  int a = 2;
  int b = 3;
  int c = 4;
  static int forceclinit = 13;

  public NewTest(int a, int b)
    { // TODO: bug in Java2Jimple, loses this try catch block 
      /* try 
        { this.a = a;
          this.b = b;
	}
      catch (Exception e)
        { this.a = 1;
	}
       */
       this.a = a;
       this.b = b;
    }

  // creating objects in args to this, make sure correct <init> is found
  public NewTest()
    { this (new Integer(2).intValue(), new Integer(3).intValue());
      this.a = 13;
    }

  // creating NewTest args in this, make sure correct <init> is found
  public NewTest(int a)
    { this (new NewTest(2,3).a, new NewTest(2,3).b);
      this.a = 13;
    }

  // creating NewTest with multiple returns
  public NewTest(int a, int b, int c)
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
        { a = x;
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
     NewTest t = new NewTest(x+1,y+2);
     t.c(0);
     t.d(1);
     t.e(2);
     t.f(3);
     t.g(4);
     t.h(5);
   }
}



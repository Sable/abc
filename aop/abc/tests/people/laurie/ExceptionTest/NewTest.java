public class NewTest {
  int a;
  int b;

  public NewTest(int a, int b)
    { // TODO: bug in Java2Jimple, loses this try catch block 
      try 
        { a = a;
          b = b;
	}
      catch (Exception e)
        { a = 1;
	}
    }

  // creating objects in args to this, make sure correct <init> is found
  public NewTest()
    { this (new Integer(2).intValue(), new Integer(3).intValue());
      a = 13;
    }

  // creating NewTest args in this, make sure correct <init> is found
  public NewTest(int a)
    { this (new NewTest(2,3).a, new NewTest(2,3).b);
      a = 13;
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
    { int sum = 0;
      do
        { sum += x;
	  x--;
	}
      while (x > 0);
      return(sum);
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



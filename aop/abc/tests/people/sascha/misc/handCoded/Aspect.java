public aspect Aspect {

  static int aspectlevel = 0;

  static void message(String s) 
    { for (int i=0; i < aspectlevel; i++) System.out.print("===+");
      System.out.println(" " + s);
    }

  before() : execution (int *..*(..)) && !within(Aspect)
    { message("beginning of method");
    }

  before () : call (int *..*(..)) && !within(Aspect)
    { message("Here is some before advice"); }

  // calls to methods
  int around(int x) : call(int *..*(..)) && !within(Aspect) && args(x,..)
    { message("1: at beginning of around, value of first param is " + x); 
      int mylocalx = x + 1;
      aspectlevel++; 
      int result = 0;
      for (int i=0; i< 10; i++)
        { if (i%2 == 0)
	    { message("if inside for");
	      int anotherlivevar = i * 2;
	      result = proceed(mylocalx + i);
	      result = result + anotherlivevar;
	    }
	  else
	    { message("else inside for");
	      result = proceed(i);
	    }
	  message("in loop " + i + " result is " + result);
	}
      message("1: at end of around, return value is " + result); 
      message("1: at end of around, mylocalx is " + mylocalx); 
      aspectlevel --;
      return(result);
    }


  /*
  int around(int x) : call(int *..*(..)) && !within(Aspect) && args(x,..)
    { message("2: before method call, value of first param is " + x); 
      aspectlevel++; 
      int r = proceed(x);
      message("2: after method call, return value is " + x); aspectlevel --;
      return(r);
    }
  */
}

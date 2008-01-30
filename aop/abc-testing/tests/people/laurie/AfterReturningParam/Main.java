public class Main {

  static int x = 13;

  int y = 2;

  double d = 1.0;

  public Main(int y)
    { this.y = y;
    }

  int f ( int i )
    {  return(i+1);
    }

  double g (double r)
    {  return(r + 1.0);
    }

  String s (String f)
    {   return(f + " hi ");
    }

  public static void main (String args[])
    {  int x = 14;
       int y = 15; 
       Main m = new Main(y + x);
       m.f(m.y);
       m.g(m.d);
       m.s(m.toString());
    }
}

aspect Aspect {

  after () returning (int x) : !within(Aspect) 
  {
     System.out.println("AFTER1: x is " + thisJoinPointStaticPart.getKind() +
	 " at " + thisJoinPointStaticPart.getSourceLocation() + " " + x);
  }

  after () returning (double x) : call(double *(..)) && !within(Aspect) 
  {
     System.out.println("AFTER2: x is " + thisJoinPointStaticPart.getKind() +
	 " at " + thisJoinPointStaticPart.getSourceLocation() + " " + x);
  }

  after () returning (Object x) : !within(Aspect) 
  {
     System.out.println("AFTER3: x is " + thisJoinPointStaticPart.getKind() +
	 " at " + thisJoinPointStaticPart.getSourceLocation() + " " + x);
  }

}

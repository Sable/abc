public class Test {

  public Test() { }

  static int outsidex = 3;
  int a = 12;

  // body ending in a goto
  public int c(int x)
    { int i1;
    
     System.out.println("I'm at the beginning of c,  being called with " + x);
    
    if (x != 0)
     { i1 = x * x; }
    else 
     { i1 = 0; }
    
    System.out.println("I'm at the end of c,  returning with " + i1);
    
    return i1;
  }


  public static void main(String args[])
   { int x = 4;
     
     Test t = new Test();
     System.out.println("--- calling c(4) --------");

     int rval;
     rval = t.c(x);

     System.out.println("\n--- calling c(5) ---------");
     int rval2;
     rval2 = t.c(x+1);
 
     System.out.println("final values are " + rval + " " + rval2);
   }
}

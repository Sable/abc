public class Test  {

    static int j;

    double r;

    static 
    { int i = 10;
      if (i == 10)
        { j = 3; }
      else
        { j = 4; } 
    }

   public Test(int x) {
      r=(double) x;
   }

   public static void main (String args[])
     { int k = new Integer(4).intValue();
       Test t = new Test(3+k);
       Test t2 = new Test(k+5);
       t.r = 10.0;
       System.out.println("j is " + j);
       System.out.println("r is " + t.r);
     }
}

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

   public static void main (String args[])
     { Test t = new Test();
       t.r = 10.0;
       System.out.println("j is " + j);
       System.out.println("r is " + t.r);
     }
}

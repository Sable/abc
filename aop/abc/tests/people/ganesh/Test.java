public class Test  {

    static int j;

    double r;

    static Test t=new Test(5);

    static 
    { int i = 10;
      if (i == 10)
        { j = 3; }
      else
        { j = 4; } 
    }

   public Test (double y) {
     r=y;
   }

   public Test(int x) {
      r=(double) x;
   }

   public Test() {
     this(t.r);
     double y=t.r;
   }

   int foo(int x) {
      return 5;
   }

   double foo(double x) {
      return x;
   }

   int bar(int x) {
      try {
         System.out.println("bar");
         return x;
      } catch(Exception e) {
         return (x+1);
      }
   }

   public static void main (String args[])
     { int k = new Integer(4).intValue();
       Test t = new Test(3+k);
       t.foo(3);
       t.bar(3);
       t.foo(3.0);
       Test t2 = new Test(k+5);
       t.r = 10.0;
          System.out.println("j is " + j);
          System.out.println("r is " + t.r);
     }
}

class Baz extends Test {

}

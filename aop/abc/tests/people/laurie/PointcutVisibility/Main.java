public class Main {
  
  pointcut inmain() : !within(Aspect*);
  
  private int x;

  private int f (int k) { return this.x + k; }

  private static void doit()
   { Main m = new Main();
     A a = new A();
     m.f(3);
     a.g(3);
    }

  public static void main(String args[])
   { doit(); }
 
}

class A {

  pointcut inA() : !within(Aspect*);

  private int y;

  private int f (int x) { return this.y + x; }

  int g(int x) { return f(x); }

}

public class ConstructorMain {
  public static void main(String args[])
    { int k = 100; 
      // These are ok, note order constructors given in class
      System.out.println("----------------------"); 
      B b3 = new B(3+k, 4+k);
      System.out.println("----------------------"); 
      B b2 = new B(2 + k);
      System.out.println("----------------------"); 
      B b1 = new B();

      // First two are ok, but last one not ok 
      System.out.println("----------------------"); 
      C c3 = new C(3+k, 4+k);
      System.out.println("----------------------"); 
      C c2 = new C(2 + k);
      System.out.println("----------------------"); 
      C c1 = new C();

      System.out.println("----------------------"); 
    }
}

class A {
  int x = 4;
  A (int x) { this.x = x; }
} 

class B extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  B (int x, int y) { super(x+y); this.y = x+y; }

  B (int x) { this(x+l, x+l); this.y = x+l; } 

  B () { this(k+j); this.y = l; }


}


class C extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  C () { this(k+j); this.y = l; }

  C (int x) { this(x+l, x+l); this.y = x+l; } 

  C (int x, int y) { super(x+y); this.y = x+y; }


}

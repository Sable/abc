public class ConstructorMain {
  public static void main(String args[])
    { int k = 100; 
      B b1 = new B();
      B b2 = new B(2 + k);
      B b3 = new B(3+k, 4+k);
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

  B () { this(k+j); this.y = l; }

  B (int x) { this(x+l, x+l); this.y = x+l; } 

  B (int x, int y) { super(x+y); this.y = x+y; }

}

package test;

public aspect Test53 {
  ast A;

  syn boolean A.x() circular [false] = y();
  syn boolean A.y() circular [false] = x() || true;

  public static void main(String[] args) {
    System.out.println("Circular: two mutually circular attributes that may require an iteration before termination");
    A node = new A();
    System.out.println("x() == true: " + (node.x() == true));
    System.out.println("y() == true: " + (node.y() == true));
  }
}

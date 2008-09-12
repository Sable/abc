package test;

public aspect Test52 {
  ast A;

  syn lazy boolean A.u() circular [false] = v();
  syn lazy boolean A.v() circular [false] = u();

  public static void main(String[] args) {
    System.out.println("Circular: two mutually circular attributes that terminate immediately");
    A node = new A();
    System.out.println("u() == false: " + (node.u() == false));
    System.out.println("v() == false: " + (node.v() == false));
  }
}

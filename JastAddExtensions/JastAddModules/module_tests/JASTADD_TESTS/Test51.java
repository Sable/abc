package test;

public aspect Test51 {
  ast A;

  syn lazy boolean A.a() circular [false] = a();

  public static void main(String[] args) {
    System.out.println("Circular: self circular synthesized attribute");
    A node = new A();
    System.out.println(node.a() == false);
  }
}

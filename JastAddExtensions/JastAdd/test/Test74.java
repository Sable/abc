package test;

public aspect Test74 {
  ast A ::= B*;
  ast B;

  public static void main(String[] args) {
    A a = new A(new List().add(new B()).add(new B()));
    System.out.println("Enhanced for using getBList()");
    for(B node : a.getBList())
      System.out.println("Found a child of type: " + node.getClass().getName());
    System.out.println("Enhanced for using getBs()");
    for(B node : a.getBs())
      System.out.println("Found a child of type: " + node.getClass().getName());
  }
}

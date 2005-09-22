package test;

public aspect Test6 {
  ast Node ::= <Value:int>;

  public static void main(String[] args) {
    System.out.println("AST: single token, explicit primitive type");
    Node n = new Node(1);
    System.out.println(n.getValue());
    n.setValue(0);
    System.out.println(n.getValue());
  }
}

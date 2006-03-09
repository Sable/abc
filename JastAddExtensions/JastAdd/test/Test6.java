package test;

public aspect Test6 {
  ast Node ::= <Value:boolean>;

  public static void main(String[] args) {
    System.out.println("AST: single token, explicit primitive type");
    Node n = new Node(true);
    System.out.println(n.getValue());
    n.setValue(false);
    System.out.println(n.getValue());
  }
}

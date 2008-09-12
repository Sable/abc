package test;

public aspect Test7 {
  ast Node ::= Node;

  public static void main(String[] args) {
    System.out.println("AST: single child");
    Node m = new Node();
    Node n = new Node(m);
    System.out.println(n.getNode() == m);
  }
}

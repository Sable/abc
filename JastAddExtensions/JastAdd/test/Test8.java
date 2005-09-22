package test;

public aspect Test8 {
  ast Node ::= MyNode:Node;

  public static void main(String[] args) {
    System.out.println("AST: single named child");
    Node m = new Node();
    Node n = new Node(m);
    System.out.println(n.getMyNode() == m);
  }
}

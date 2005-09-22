package test;

public aspect Test11 {
  ast Node ::= Node*;

  public static void main(String[] args) {
    System.out.println("AST: list of children");
    Node first = new Node();
    Node second = new Node();
    System.out.println("Creating list with two children");
    Node n = new Node(new List().add(first).add(second));
    System.out.println("size: " + n.getNumNode());
    System.out.println("First: " + (n.getNode(0) == first));
    System.out.println("Second: " + (n.getNode(1) == second));
  }
}

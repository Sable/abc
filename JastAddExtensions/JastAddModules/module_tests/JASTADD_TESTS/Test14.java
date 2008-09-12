package test;

public aspect Test14 {
  ast Node ::= [Node];

  public static void main(String[] args) {
    System.out.println("AST: optional child");
    Node n = new Node();
    System.out.println("HasNode: " + n.hasNode());
    Node m = new Node();
    n.setNode(m);
    System.out.println("Adding node");
    System.out.println("HasNode: " + n.hasNode());
    System.out.println("Correct node: " + (n.getNode() == m));
  }
}

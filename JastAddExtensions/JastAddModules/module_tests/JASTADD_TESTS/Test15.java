package test;

public aspect Test15 {
  ast Node ::= [MyNode:Node];

  public static void main(String[] args) {
    System.out.println("AST: optional named child");
    Node n = new Node();
    System.out.println("HasNode: " + n.hasMyNode());
    Node m = new Node();
    n.setMyNode(m);
    System.out.println("Adding node");
    System.out.println("HasNode: " + n.hasMyNode());
    System.out.println("Correct node: " + (n.getMyNode() == m));
  }
}

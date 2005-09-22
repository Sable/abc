package test;

public aspect Test12 {
  ast Node ::= Node*;

  public static void main(String[] args) {
    System.out.println("AST: adding nodes to a list of children");
    Node first = new Node();
    Node second = new Node();
    System.out.println("Creating list with two children");
    Node n = new Node(new List().add(first).add(second));
    System.out.println("size: " + n.getNumNode());
    System.out.println("First: " + (n.getNode(0) == first));
    System.out.println("Second: " + (n.getNode(1) == second));
    System.out.println("Adding one child");
    Node third = new Node();
    n.addNode(third);
    System.out.println("size: " + n.getNumNode());
    System.out.println("First: " + (n.getNode(0) == first));
    System.out.println("Second: " + (n.getNode(1) == second));
    System.out.println("Third: " + (n.getNode(2) == third));
    System.out.println("Adding another child");
    Node fourth = new Node();
    n.addNode(fourth);
    System.out.println("size: " + n.getNumNode());
    System.out.println("First: " + (n.getNode(0) == first));
    System.out.println("Second: " + (n.getNode(1) == second));
    System.out.println("Third: " + (n.getNode(2) == third));
    System.out.println("Fourth: " + (n.getNode(3) == fourth));
  }
}

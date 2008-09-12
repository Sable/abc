package test;

public aspect Test10 {
  ast Node ::= Left:Node Right:Node;

  public static void main(String[] args) {
    System.out.println("AST: multiple named children");
    Node left = new Node();
    Node right = new Node();
    Node n = new Node(left, right);
    System.out.println("Left: " + (n.getLeft() == left));
    System.out.println("Right: " + (n.getRight() == right));
  }
}

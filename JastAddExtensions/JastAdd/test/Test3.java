package test;

public aspect Test3 {
  ast Node;

  public static void main(String[] args) {
    System.out.println("AST: implicit ASTNode inheritance");
    Node n = new Node();
    System.out.println(n.getNumChild());
  }
}

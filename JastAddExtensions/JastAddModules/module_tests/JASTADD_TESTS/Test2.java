package test;

public aspect Test2 {
  ast Node : ASTNode;

  public static void main(String[] args) {
    System.out.println("AST: explicit ASTNode inheritance");
    Node n = new Node();
    System.out.println(n.getNumChild());
  }
}

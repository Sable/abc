package test;

public aspect Test9 {
  ast Node ::= Node Node;

  public static void main(String[] args) {
    System.out.println("AST: multiple nodes of the same type");
  }
}

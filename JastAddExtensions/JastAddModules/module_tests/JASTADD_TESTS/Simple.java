package test;

aspect Simple {
  ast ANode : ASTNode ::= Child:ASTNode BNode [OptChild:ASTNode] B:BNode*;
  ast BNode : ANode ::= B:BNode* <Name:String> ;

  public syn int ANode.num() {
    System.out.println("ASTNode.num() computed");
    return 5;
  }

  public syn int BNode.num() {
    System.out.println("SubNode.num() computed");
    return 3;
  }

  public syn int ANode.num(int i) {
    System.out.println("ASTNode.num(int i) computed");
    return i + 1;
  }
  public syn int ANode.num(int i, String s) {
    return i;
  }

  public static void main(String[] args) {
    System.out.println("Hello");
    ANode n = new ANode();
    System.out.println(n.num());
    System.out.println(n.num());
    System.out.println(n.num(1));
    System.out.println(n.num(1));
    ANode s = new BNode();
    System.out.println(s.num());
    System.out.println(s.num());
    BNode b = new BNode();
    b.setName("name");
    System.out.println(b.getName());
    //System.out.println("Child: " + n.getChild());
    //System.out.println("SubNode: " + n.getBNode());
    System.out.println("hasOptChild: " + n.hasOptChild());
    System.out.println("numSub: " + n.getNumB());
    System.out.println("Bye");
  }
}

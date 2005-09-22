package test;

public aspect Test13 {
  ast Node ::= Node* <Name>;

  public static void main(String[] args) {
    System.out.println("AST: inserting nodes in a list of children");
    Node n = new  Node(new List(), "root");
    
    System.out.println("Insert A in empty list");
    Node A = new Node();
    A.setName("A");
    n.insertNode(A, 0);
    System.out.println("size: " + n.getNumNode());
    for(int i = 0; i < n.getNumNode(); i++)
      System.out.println(n.getNode(i).getName());
    
    System.out.println("Insert B first in list");
    Node B = new Node();
    B.setName("B");
    n.insertNode(B, 0);
    System.out.println("size: " + n.getNumNode());
    for(int i = 0; i < n.getNumNode(); i++)
      System.out.println(n.getNode(i).getName());
    
    System.out.println("Insert C last in list");
    Node C = new Node();
    C.setName("C");
    n.insertNode(C, n.getNumNode());
    System.out.println("size: " + n.getNumNode());
    for(int i = 0; i < n.getNumNode(); i++)
      System.out.println(n.getNode(i).getName());
    
    System.out.println("Insert D at position 2");
    Node D = new Node();
    D.setName("D");
    n.insertNode(D, 2);
    System.out.println("size: " + n.getNumNode());
    for(int i = 0; i < n.getNumNode(); i++)
      System.out.println(n.getNode(i).getName());
    
    System.out.println("Insert E at erroneous position (after node getNumNode())");
    Node E = new Node();
    E.setName("E");
    try {
      n.insertNode(E, n.getNumNode()+1);
    } catch (Error e) {
      System.out.println("Error caught: " + e.getMessage());
    }
  }
}

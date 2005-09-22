package test;

public aspect Test16 {
  ast Node;

  syn boolean Node.attr() = true;

  public static void main(String[] args) {
    System.out.println("Syn: synthesized attribute with initializing equation");
    Node node = new Node();
    System.out.println("Attribute value: " + node.attr());
  }
}

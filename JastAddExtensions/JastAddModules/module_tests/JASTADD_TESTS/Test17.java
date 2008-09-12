package test;

public aspect Test17 {
  ast Node;
  ast SubNode : Node;

  syn boolean Node.attr() = true;
  syn boolean SubNode.attr() = false;

  public static void main(String[] args) {
    System.out.println("Syn: synthesized attribute with initializing equation, overriden in subclass");
    Node node = new Node();
    System.out.println("Attribute value: " + node.attr());
    node = new SubNode();
    System.out.println("Attribute value: " + node.attr());
  }
}

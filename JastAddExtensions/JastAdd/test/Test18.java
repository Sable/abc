package test;

public aspect Test18 {
  ast Node;
  ast SubNode : Node;

  syn boolean Node.attr();
  eq Node.attr() = true;
  eq SubNode.attr() = false;

  syn int Node.value();
  eq Node.value() = 1;
  eq SubNode.value() = 2;

  public static void main(String[] args) {
    System.out.println("Syn: synthesized attribute with initializing equation, overriden in subclass");
    Node node = new Node();
    System.out.println("Attribute attr: " + node.attr());
    System.out.println("Attribute value: " + node.value());
    node = new SubNode();
    System.out.println("Attribute attr: " + node.attr());
    System.out.println("Attribute value: " + node.value());
  }
}

package test;

public aspect Test19 {
  abstract ast Node;
  ast SubNode : Node;

  syn boolean Node.attr();
  eq SubNode.attr() = false;

  public static void main(String[] args) {
    System.out.println("Syn: synthesized attribute in abstract class with overriding equation in subclass");
    Node node = new SubNode();
    System.out.println("Attribute attr: " + node.attr());
  }
}

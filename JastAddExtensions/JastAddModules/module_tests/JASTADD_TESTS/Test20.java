package test;

public aspect Test20 {
  ast Node;
  ast SubNode : Node;

  syn String Node.attr(String name) = name;
  eq SubNode.attr(String name) = "Sub" + name;

  public static void main(String[] args) {
    System.out.println("Syn: parameterized synthesized attribute with overriding equation in subclass");
    Node node = new Node();
    System.out.println(node.attr("A"));
    System.out.println(node.attr("B"));
    node = new SubNode();
    System.out.println(node.attr("A"));
    System.out.println(node.attr("B"));
  }
}

package test;

public aspect Test78 {
  ast A ::= Left:B Right:B;
  ast SubA : A ::= /Left:B/ Right:B;
  ast B;

  syn B SubA.getLeft() = new B();

  inh boolean B.value();
  eq A.getLeft().value() = true;
  eq A.getChild().value() = false;

  public static void main(String[] args) {
    A a = new SubA(new B());
    System.out.println(a.getRight().value());
    System.out.println(a.getLeft().value());
    System.out.println(a.getNumChild());
  }
}

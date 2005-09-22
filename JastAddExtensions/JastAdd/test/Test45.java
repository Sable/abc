package test;

public aspect Test45 {
  ast A ::= B*;
  abstract ast B;
  ast C : B;
  ast D : B;

  rewrite C in A.getB() {
    when(true)
    to List {
      List newList = new List().add(
        new D()
      ).add(
        new D()
      );
      return newList;
    }
  }

  public static void main(String[] args) {
    System.out.println("Rewrites: rewrite each C in A's B-list to two Ds");
    A a = new A(new List().add(new C()));
    for(int i = 0; i < a.getNumB(); i++)
      System.out.println(a.getB(i).getClass().getName());
  }
}

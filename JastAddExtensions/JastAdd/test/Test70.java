package test;

public aspect Test70 {
  ast X ::= Y Z;
  ast Y;
  ast Z;

  inh String Z.name(String s);
  inh String Y.name(String name);

  eq X.getChild().name(String w) = w;

  public static void main(String[] args) {
    X x = new X(new Y(), new Z());
    System.out.println(x.getY().name("y"));
    System.out.println(x.getY().name("z"));
  }
}
    

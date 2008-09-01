package test;

public aspect Test75 {
  ast A ::= /B/ /BFinal:B/;
  ast B;

  syn lazy B A.getB() = new B();
  syn lazy final B A.getBFinal() = new B();

  syn nta B A.b() = new B();
  syn nta B A.selectB(int i) = new B();

  syn lazy Object A.object() = new Object();
  syn lazy final Object A.objectFinal() = new Object();

  inh int B.value();
  eq A.getChild().value() = -1;
  eq A.selectB(int i).value() = i;

  public static void main(String[] args) {
    A a = new A();
    a.is$Final(false);

    if(a.getB() == a.getB())
      System.out.println("Non final grammar nta is cached");
    else
      System.out.println("Non final grammar nta is not cached");

    if(a.getBFinal() == a.getBFinal())
      System.out.println("Final grammar nta is cached");
    else
      System.out.println("Final grammar nta is not cached");

    if(a.b() == a.b())
      System.out.println("Declared nta is cached");
    else
      System.out.println("Declared nta is not cached");

    if(a.selectB(0) == a.selectB(0) &&
       a.selectB(0) != a.selectB(1) &&
       a.selectB(1) == a.selectB(1))
      System.out.println("Declared indexed nta is cached");
    else
      System.out.println("Declared indexed nta is not cached");

    if(a.object() == a.object())
      System.out.println("Non final attribute is cached");
    else
      System.out.println("Non final attribute is not cached");

    if(a.objectFinal() == a.objectFinal())
      System.out.println("Final attribute is cached");
    else
      System.out.println("Final attribute is not cached");

    System.out.println(a.getBFinal().value());
    System.out.println(a.b().value());
    System.out.println(a.selectB(0).value());
    System.out.println(a.selectB(1).value());
  }
}

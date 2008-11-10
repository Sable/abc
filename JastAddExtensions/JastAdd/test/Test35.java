package test;

public aspect Test35 {
  ast A ::= B;
  ast B;

  syn boolean A.syn1() {
    System.out.println("Non lazy syn computed");
    return true;
  } 
  
  syn lazy boolean A.syn2() {
    System.out.println("Lazy syn computed");
    return true;
  } 

  inh boolean B.inh1();
  eq A.getB().inh1() {
    System.out.println("Non lazy inh computed");
    return true;
  }
  
  inh lazy boolean B.inh2();
  eq A.getB().inh2() {
    System.out.println("Lazy inh computed");
    return true;
  }
  
  public static void main(String[] args) {
    System.out.println("LAZY: check that lazy is used for syn and inh decl");
    A a = new A(new B());
    a.is$Final = true;
    B b = a.getB();

    a.syn1();
    a.syn1();
    a.syn2();
    a.syn2();

    b.inh1();
    b.inh1();
    b.inh2();
    b.inh2();
  }
}

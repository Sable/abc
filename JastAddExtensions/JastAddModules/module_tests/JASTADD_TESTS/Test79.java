package test;

public aspect Test79 {
  ast Program ::= A;
  ast A;
  
  syn nta A A.first() = new A();
  syn nta String A.second() = "";
  syn nta ASTNode A.third() = new ASTNode();

  inh nta A A.inhFirst();
  inh nta String A.inhSecond();
  inh nta ASTNode A.inhThird();

  eq Program.getChild().inhFirst() = new A();
  eq Program.getChild().inhSecond() = "";
  eq Program.getChild().inhThird() = new ASTNode();

  public static void main(String[] args) {
    // Declared nta:s must be subtypes of ASTNode
  }
}

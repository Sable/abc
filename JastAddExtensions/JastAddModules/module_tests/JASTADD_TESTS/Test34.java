package test;

public aspect Test34 {
  ast Program ::= A <Name> /*NonTerminalA:A*/;
  ast SubProgram : Program ::= As:A*;
  ast A;

  public static void main(String[] args) {
    System.out.println("Nodecopy: check that getNumChild() works 'as expected'");
    Program p1 = new Program();
    Program p2 = new SubProgram();
    System.out.println("Program has " + p1.getNumChild() + " children");
    System.out.println("SubProgram has " + p2.getNumChild() + " children");
  }
}

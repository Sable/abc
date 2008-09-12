package test;

public aspect Test68 {
  ast X ::= Y*;
  ast Y;

  static aspect A1 {
    inh String Y.m();
    eq X.getChild(int i).m() {
      return "X[" + i + "].m() in A1";
    }
  }
  static aspect A2 {
    refine A1 eq X.getChild(int index).m() {
      return "X[" + index + "].m() in A2 + " + refined();
    }
  }
  public static void main(String[] args) {
    // Refinement of inh equations that uses refined() to invoke old implementation
    System.out.println(new X(new List().add(new Y())).getY(0).m());
  }
}

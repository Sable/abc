package test;

public aspect Test66 {
  ast X ::= Y;
  ast Y;

  static aspect A1 {
    inh String Y.m();
    eq X.getY().m() {
      return "X.m() in A1";
    }
  }
  static aspect A2 {
    refine A1 eq X.getY().m() {
      return "X.m() in A2 + " + refined();
    }
  }
  public static void main(String[] args) {
    // Refinement of inh equations that uses refined() to invoke old implementation
    System.out.println(new X(new Y()).getY().m());
  }
}

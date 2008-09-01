package test;

public aspect Test65 {
  ast X;

  static aspect A1 {
    public syn String X.m();
    eq X.m() {
      System.out.println("X.m() in A1");
      return "";
    }

    public syn int X.n() = 2;
  }
  static aspect A2 {
    refine A1 eq X.m() {
      System.out.println("X.m() in A2");
      return refined();
    }
    refine A1 eq X.n() {
      return refined() + 1;
    }
  }
  public static void main(String[] args) {
    // Refinement of syn equations that uses refined() to invoke old implementation
    new X().m();
    System.out.println(new X().n());
  }
}

package test;

import java.util.*;

public aspect Test71 {
  ast A ::= B*;
  ast B;

  inh A B.a();
  eq A.getB().a() {
    System.out.println("Computing a()");
    return this;
  }

  coll Collection<B> A.set() [new HashSet<B>()] with add root A;
  B contributes
    computeContribution() when true
  to A.set() for a();

  syn B B.computeContribution() {
    System.out.println("computeContribution");
    return this;
  }

  public static void main(String[] args) {
    // testing collection attributes
    A a = new A(new test.List().add(new B()).add(new B()));
    for(B b : a.set())
      System.out.println("Found a B");
  }
}

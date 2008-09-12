package test;

import java.util.*;

public aspect Test72 {
  ast A ::= B*;
  ast B;

  inh A B.a();
  eq A.getB().a() = this;

  syn B B.b() = this;

  // HashSet<A> is not a subtype of Collection<B>
  // there is no root node named X
  // it should be with rather than woth
  // it should be root rather than ruot
  coll Collection<B> A.set() [new HashSet<A>()] woth add ruot X;
  // contribution is not a subtype of B
  // condition is not boolean
  // b() does not bind to an A node
  B contributes
    5 when 0
  to A.set() for b();
  // there is not collection attribute named set2 in A
  B contributes
    this
  to A.set2() for a();

  // there is not a method named adds taking one argument of type B
  coll Collection<B> A.s() [new HashSet<B>()] with adds root A;

  public static void main(String[] args) {
    // testing various errors for collection attributes
    A a = new A(new test.List().add(new B()).add(new B()));
    for(B b : a.set())
      System.out.println("Found a B");
  }
}

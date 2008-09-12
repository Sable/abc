package test;

import java.util.*;

public aspect Test76 {
  ast A;

  syn lazy HashSet ASTNode.emptyHashSet() = new HashSet();

  syn HashSet A.m() circular [emptyHashSet()] {
    return this.m();
  }

  syn HashSet A.n() circular [this.emptyHashSet()] {
    return this.n();
  }

  public static void main(String[] args) {
    A a = new A();
    if(a.m() == a.emptyHashSet())
      System.out.println("Found circularly computed hash set");
    if(a.n() == a.emptyHashSet())
      System.out.println("Found circularly computed hash set");
  }
}


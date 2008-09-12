package test;

public aspect Test73 { 
  // ensure that attributes and rewrites only operate on AST nodes
  static class X { }
  syn boolean X.m();
  inh boolean X.n();
  rewrite X {
    to X new X();
  }
  coll java.util.ArrayList X.c() [new java.util.ArrayList()] with add root X;
}

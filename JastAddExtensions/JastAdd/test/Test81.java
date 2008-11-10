package test;

import java.util.HashSet;

public aspect Test81 {
  ast Program1 ::= Stmt;
  ast Program2 ::= Stmt;
  ast Stmt;

  coll HashSet<String> Stmt.strings() [new HashSet<String>()] with add;

  public static void main(String[] args) {
    Program1 p1 = new Program1(new Stmt());
    Program2 p2 = new Program2(new Stmt());
  }
}


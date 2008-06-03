package test;

import AST.*;

public class Example01 {
  public static void main(String[] args) {
    Expr s = #Expr "hello";
    Expr e = #Expr x.out.println(# s);
  }
}

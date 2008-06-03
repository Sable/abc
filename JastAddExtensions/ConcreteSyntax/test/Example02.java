package test;
import AST.*;
public class Example02 {
  public static void main(String[] args) {
    Expr s = 
  new AST.StringLiteral("hello");
    Expr e = 
  new AST.Dot(
    new AST.TypeAccess("System"),
    new AST.Dot(
      new AST.VarAccess("out"),
      new AST.MethodAccess(
        "println",
        new AST.List().add(
s
        )
      )
    )
  );
  }
}

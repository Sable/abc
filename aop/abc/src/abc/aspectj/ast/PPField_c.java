package abc.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.Field_c;

public class PPField_c extends Field_c implements Field
{

  public PPField_c(Position pos, Receiver target, String name) {
    super(pos,target,name);
  }

     /** Write the field to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    if (!targetImplicit) {
        // explicit target.
        if (target instanceof Expr) {
          printSubExpr((Expr) target, w, tr);
        }
        else if (target instanceof TypeNode) {
          print(target, w, tr);
        }
        else print(target,w,tr);  // patch by ODM
    
        w.write(".");
    }
    w.write(name);
  }
  
  
}

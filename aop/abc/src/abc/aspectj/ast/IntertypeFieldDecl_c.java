package arc.aspectj.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.ast.Expr;
import polyglot.types.Flags;
import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;

import polyglot.ext.jl.ast.FieldDecl_c;


public class IntertypeFieldDecl_c extends FieldDecl_c
                                  implements IntertypeFieldDecl
{
    protected TypeNode host;

    public IntertypeFieldDecl_c(Position pos,
                                Flags flags,
                                TypeNode type,
                                TypeNode host,
                                String name,
                                Expr init) {
	super(pos,flags,type,name,init);
	this.host = host;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(flags().translate());
        print(type(), w, tr);
        w.write(" ");
        print(host, w, tr);
        w.write(".");
        w.write(name());

        if (init() != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init(), w, tr);
        }

        w.write(";");
    }

}
	

	

     



/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * @author Neil Ongkingco
 *
 */
public class ModMemberModule_c extends Node_c implements ModMemberModule {

    private String name;
    private boolean isConstrained = false;
    
    public ModMemberModule_c(Position pos, String name, boolean isConstrained) {
        super(pos);
        this.name = name;
        this.isConstrained = isConstrained;
    }
    
    public boolean isConstrained() {
        return isConstrained;
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("module " + name);
        w.newline();
        //super.prettyPrint(w, pp);
    }
    
    public String name() {
        return name;
    }
    
}

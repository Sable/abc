/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.List;

import abc.aspectj.ast.*;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.*;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * @author Neil Ongkingco
 *  
 */
public class ModMemberClass_c extends Node_c implements ModMemberClass {

    private ClassnamePatternExpr cpExpr;

    public ModMemberClass_c(Position pos, ClassnamePatternExpr cpExpr) {
        super(pos);
        this.cpExpr = cpExpr;
    }

    public ClassnamePatternExpr getCPE() {
        return cpExpr;
    }
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("class ");
        cpExpr.prettyPrint(w, pp);
        w.newline();
    }

    private ModMemberClass_c reconstruct(ClassnamePatternExpr cpExpr) {
        if (cpExpr != this.cpExpr) {
            ModMemberClass_c n = (ModMemberClass_c) copy();
            n.cpExpr = cpExpr;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {

        ClassnamePatternExpr cpe = (ClassnamePatternExpr) visitChild(cpExpr, v);
        
        return reconstruct(cpe);
    }

}
/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.List;

import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.ast.SimpleNamePattern_c;
import abc.aspectj.visit.ContainsNamePattern;

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
public class ModMemberAspect_c extends Node_c implements ModMemberAspect,
        ContainsNamePattern {

    private String name;
    private NamePattern namePattern;

    public ModMemberAspect_c(Position pos, String name) {
        super(pos);
        this.name = name;
        this.namePattern = new SimpleNamePattern_c(pos, name);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("aspect " + name);
        w.newline();
    }

    public String name() {
        return name;
    }
    
    public NamePattern getNamePattern() {
        return namePattern;
    }

    private ModMemberAspect_c reconstruct(NamePattern namePattern) {
        if (namePattern != this.namePattern) {
            ModMemberAspect_c n = (ModMemberAspect_c) copy();
            n.name = this.name;
            n.namePattern = namePattern;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {

        NamePattern namePattern = (NamePattern) visitChild(this.namePattern, v);
        
        return reconstruct(namePattern);
    }

}
/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.*;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.ext.jl.ast.Term_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * @author Neil Ongkingco 
 *
 */
public class SigMemberPCDecl_c extends Node_c implements SigMemberPCDecl, MakesAspectMethods {

    private abc.aspectj.ast.Pointcut pc;
    private boolean isPrivate = false;
    
    public SigMemberPCDecl_c(polyglot.util.Position pos, abc.aspectj.ast.Pointcut pc, boolean isPrivate) {
        super(pos);
        this.pc = pc;
        this.isPrivate = isPrivate;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public Pointcut getAIPointcut() {
        return this.pc.makeAIPointcut();
    }
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        pc.prettyPrint(w, pp);
        w.newline();
        //super.prettyPrint(w, pp);
    }    
    
    public SigMemberPCDecl_c reconstruct(abc.aspectj.ast.Pointcut pc) {
        if (pc != this.pc) {
            SigMemberPCDecl_c n = (SigMemberPCDecl_c)copy();
            n.pc = pc;
            return n;
        }
        return this;
    }
    
    public Node visitChildren(NodeVisitor v) {
        abc.aspectj.ast.Pointcut pc = 
            (abc.aspectj.ast.Pointcut)visitChild(this.pc, v);
        return reconstruct(pc);
    }

    public void aspectMethodsEnter(AspectMethods visitor) {
        //push an empty list of formals. needed for If pointcuts
        visitor.pushFormals(Collections.unmodifiableList(new LinkedList()));
    }
    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
            AJTypeSystem ts) {
        return this;
    }
}

package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import abc.weaving.aspectinfo.AbcFactory;

public class DeclareParentsImpl_c extends DeclareDecl_c 
    implements DeclareParentsImpl, ContainsAspectInfo
{

    ClassnamePatternExpr pat;
    TypedList interfaces;

    Collection/*<AbcClass>*/ targets = new ArrayList();

    public DeclareParentsImpl_c(Position pos, 
                               ClassnamePatternExpr pat,
                               List interfaces)
    {
	super(pos);
        this.pat  = pat;
        this.interfaces = TypedList.copyAndCheck(interfaces,
                                                 TypeNode.class,
                                                 true);
    }

    protected DeclareParentsImpl_c reconstruct(ClassnamePatternExpr pat,
					       TypedList interfaces) {
	if (pat != this.pat || !CollectionUtil.equals(interfaces, this.interfaces)) {
	    DeclareParentsImpl_c n = (DeclareParentsImpl_c) copy();
	    n.pat = pat;
	    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr pat = (ClassnamePatternExpr) visitChild(this.pat, v);
	TypedList interfaces = new TypedList(visitList(this.interfaces, v), TypeNode.class, true);
	return reconstruct(pat, interfaces);
    }

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException {
	TypedList interfaces_disam = new TypedList(new ArrayList(), TypeNode.class, false);
	Iterator ti = interfaces.iterator();
	while (ti.hasNext()) {
	    TypeNode tn = (TypeNode)ti.next();
	    tn = (TypeNode)ar.disamb(tn);
	    interfaces_disam.add(tn);
	}
	interfaces = interfaces_disam;
	return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" implements ");
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
           TypeNode tn = (TypeNode) i.next();
           print(tn, w, tr);

           if (i.hasNext()) {
                w.write (", ");
           }
        }
        w.write(";");
    }

    public ClassnamePatternExpr pat() {
	return pat;
    }

    public List/*<TypeNode>*/ interfaces() {
	return interfaces;
    }

    public void addTarget(AbcClass cl) {
	targets.add(cl);
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	//System.out.println("Declare parents impl");
	List/*<AbcClass>*/ ints = new ArrayList();
	Iterator ii = interfaces.iterator();
	while (ii.hasNext()) {
	    TypeNode i = (TypeNode)ii.next();
	    ints.add(AbcFactory.AbcClass((ClassType)i.type()));
	}
	gai.addDeclareParents(new abc.weaving.aspectinfo.DeclareParentsImpl
			      (pat.makeAIClassnamePattern(),
			       targets,
			       ints,
			       current_aspect,
			       position()));
    }

}

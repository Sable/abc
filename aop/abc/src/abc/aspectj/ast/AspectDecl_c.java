package arc.aspectj.ast;


import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ClassDecl;
import polyglot.ast.MethodDecl;

import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.util.CodeWriter;

import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.types.SemanticException;
import polyglot.types.Type;

import polyglot.ext.jl.ast.ClassDecl_c;

import arc.aspectj.types.AspectJFlags;
import arc.aspectj.types.AspectJTypeSystem;

/**
 * A <code>AspectDecl</code> is the definition of an aspect, abstract aspect,
 * or privileged. It may be a public or other top-level aspect, or an inner
 * named aspect.
 */
public class AspectDecl_c extends ClassDecl_c implements AspectDecl
{
    
    protected PerClause per;

    public AspectDecl_c(Position pos, boolean privileged, Flags flags, String name,
                        TypeNode superClass, List interfaces, PerClause per, AspectBody body) {
	     super(pos,
	           AspectJFlags.aspect(privileged ? AspectJFlags.privileged(flags): flags),
	           name,superClass,interfaces,body);
         this.per = per;
    }
    
    // add the aspectOf method to the aspect class
    // it potentially throws org.aspectj.lang.NoAspectBoundException, so that is loaded,
    // if it is a per-object associated aspect, it takes one parameter of type Object, otherwise none
	public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
		NodeFactory nf = tb.nodeFactory();
		TypeNode tn = nf.AmbTypeNode(position(),name());
		Block bl = nf.Block(position());
		AspectJTypeSystem ts = (AspectJTypeSystem) tb.typeSystem();
		TypeNode nab = nf.CanonicalTypeNode(position(),ts.NoAspectBound());
		List args = new LinkedList();
		if (per instanceof PerThis || per instanceof PerTarget) {
			TypeNode obj = nf.CanonicalTypeNode(position(),ts.Object());
			args.add(obj);
		}
		List thrws = new LinkedList(); thrws.add(nab);
		MethodDecl md = nf.MethodDecl(position(),Flags.PUBLIC.Static(),tn,"aspectOf",args,thrws,bl);       
		body = body().addMember(md); // against the polyglot doctrine of functional rewrites...
		return super.buildTypesEnter(tb);     
	}
		
	public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
		
		    // need to overwrite, because ClassDecl_c only knows of interfaces and classes
			w.write(AspectJFlags.clearAspect(flags).translate());
	        w.write("aspect ");

			w.write(name);

			if (superClass() != null) {
				w.write(" extends ");
				print(superClass(), w, tr);
			}

			if (! interfaces.isEmpty()) {
				if (flags.isInterface()) {
					w.write(" extends ");
				}
				else {
					w.write(" implements ");
				}

				for (Iterator i = interfaces().iterator(); i.hasNext(); ) {
					TypeNode tn = (TypeNode) i.next();
					print(tn, w, tr);

					if (i.hasNext()) {
						w.write (", ");
					}
				}
			}

			w.write(" {");
		}


}

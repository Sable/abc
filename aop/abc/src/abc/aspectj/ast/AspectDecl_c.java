package polyglot.ext.aspectj.ast;


import java.util.List;
import java.util.Iterator;

import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;
import polyglot.ext.jl.ast.ClassDecl_c;

import polyglot.ext.aspectj.types.AspectJFlags;

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

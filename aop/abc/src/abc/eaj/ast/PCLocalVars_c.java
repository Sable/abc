/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.AspectMethods;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class PCLocalVars_c extends Pointcut_c
                           implements PCLocalVars
{
    protected List formals;
    protected Pointcut pointcut;
    
    public Set pcRefs() {
    	return new HashSet();
    }

    public String toString()
    {
        String s = "";

        for (Iterator i = formals.iterator(); i.hasNext(); ) {
            Formal f = (Formal) i.next();
            s += f.toString();

            if (i.hasNext())
                s += ", ";
        }

        return "private (" + s + ") (...)";
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp)
    {
        w.write("private(");

        // write formals
        w.begin(0);
        for (Iterator i = formals.iterator(); i.hasNext(); ) {
            Formal f = (Formal) i.next();

            print(f, w, pp);
            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.end();

        w.write(") (");
        w.allowBreak(2, "");

        // write pointcut
        w.begin(2);
        printBlock(pointcut, w, pp);
        w.end();

        w.write(")");
    }

    public PCLocalVars_c(Position pos, List formals, Pointcut pointcut)
    {
        super(pos);
        this.formals = formals;
        this.pointcut = pointcut;
    }

    public Precedence precedence()
    {
        return pointcut.precedence();
    }

    public Collection mayBind() throws SemanticException
    {
        Collection results = pointcut.mayBind();
        Formal f;
        Iterator i = formals.iterator();

        while (i.hasNext()) {
                f = (Formal) i.next();
                if (!results.remove(f.name()))
                    throw new SemanticException("Formal \"" + f.name() +
                                           "\" is unbound in pointcut.",
                                           f.position());
        }
        return results;
    }

    public Collection mustBind()
    {
        Collection results = pointcut.mustBind();
        Formal f;
        Iterator i = formals.iterator();

        while (i.hasNext()) {
                f = (Formal) i.next();
                results.remove(f.name());
        }
        return results;
    }

    protected Node reconstruct(List formals, Pointcut pointcut)
    {
        if (!CollectionUtil.equals(formals, this.formals) ||
                pointcut != this.pointcut)
        {
            PCLocalVars_c n = (PCLocalVars_c) copy();
            n.formals = formals;
            n.pointcut = pointcut;
            return n;
        }
        return this;
    }

/** Add declarations of the variables which are local to this pointcut */
    public Context enterScope(Context c)
    {
        Context nc = super.enterScope(c);
        return nc.pushStatic();
    }

    public Node visitChildren(NodeVisitor v)
    {
        List formals = visitList(this.formals, v);
        Pointcut pointcut = (Pointcut) visitChild(this.pointcut, v);
        return reconstruct(formals, pointcut);
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut()
    {
        // Convert from polyglot Formals to weaving Formals
        List wfs = new ArrayList(formals.size());
        Iterator i = formals.iterator();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            wfs.add(new abc.weaving.aspectinfo.Formal(
                        abc.weaving.aspectinfo.AbcFactory.AbcType(
                                f.type().type()),
                                f.name(),
                                position()
                   ));
        }
        
        return new abc.weaving.aspectinfo.LocalPointcutVars(
                               pointcut.makeAIPointcut(), wfs, position());
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // Push a new list of formals comprising of the current
        // ones and the new local ones (scoping lexically)

        HashSet present = new HashSet();
        LinkedList newformals = new LinkedList(formals);

        // make a set of the new formal names
        Iterator i = newformals.iterator();
        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            present.add(f.name());
        }

        // add the formals from the current scope, unless
        // there is a local of the same name
        i = visitor.formals().iterator();
        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (!present.contains(f.name()))
                newformals.add(f);
        }

        visitor.pushFormals(newformals);
    }

    public boolean isDynamic() {
	return pointcut.isDynamic();
    }


    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        visitor.popFormals();
        return this;
    }
}

/* abc - The AspectBench Compiler
 * Copyright (C) 2006
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.om.ast;

import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ClassnamePatternExpr_c;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.aspectj.visit.PatternMatcher;

/**
 * @author Neil Ongkingco
 * 
 */
public class CPEFlags_c extends ClassnamePatternExpr_c implements CPEFlags {
    protected Flags flags;

    protected ClassnamePatternExpr cpe;

    public CPEFlags_c(Flags flags, ClassnamePatternExpr cpe, Position pos) {
        super(pos);
        this.flags = flags;
        this.cpe = cpe;
    }

    public ClassnamePatternExpr getCpe() {
        return this.cpe;
    }

    public Flags getFlags() {
        return this.flags;
    }

    protected CPEFlags_c reconstruct(ClassnamePatternExpr cpe) {
        if (cpe != this.cpe) {
            CPEFlags_c n = (CPEFlags_c) copy();
            n.cpe = cpe;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {
        ClassnamePatternExpr cpe = (ClassnamePatternExpr) visitChild(this.cpe,
                v);
        return reconstruct(cpe);
    }

    public Precedence precedence() {
        return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(flags.toString());
        printSubExpr(cpe, true, w, tr);
    }

    public String toString() {
        return flags.toString() + cpe;
    }

    //TODO: This does not yet work in the back end, later passes clean out the
    //flags if aspect classes. This means that statements such as 
    //	advertise to !(privileged *): call(* foo())
    //are allowed by the language, but do not conform to the expected semantics.
    public boolean matches(PatternMatcher matcher, PCNode cl) {
        //check if cl matches the flags
        ClassType ct = PCStructure.v().getClassType(cl);
        //assert (ct != null) : "ClassType not found. Possible PCStructure corruption";
        if (!equalFlags(flags, ct.flags())) {
            return false;
        }
        return cpe.matches(matcher, cl);
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
        if (otherexp.getClass() == this.getClass()) {
            CPEFlags otherCPEFlags = (CPEFlags) otherexp;
            return equalFlags(flags, otherCPEFlags.getFlags())
                    && (cpe.equivalent(otherCPEFlags.getCpe()));
        } else {
            return false;
        }
    }
    
    private static boolean equalFlags(Flags a, Flags b) {
        return a.contains(b) && b.contains(a);
    }

}

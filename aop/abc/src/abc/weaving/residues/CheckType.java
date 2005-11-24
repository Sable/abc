/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.*;

/** Check the type of a context value
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 30-Apr-04
 */

public class CheckType extends Residue {
    private ContextValue value;
    private Type type;
    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        return construct(value.inline(cim), type);
    }

    private CheckType(ContextValue value,Type type) {
        this.value=value;
        this.type=type;
    }

    // It's important that we throw away statically invalid matches
    // here rather than at code generation time, because if we wait until
    // then the code for a corresponding Bind will probably be generated
    // as well, and will be type incorrect; although it will be dead code,
    // the Soot code generator still won't be happy.

    public static Residue construct(ContextValue value,Type type) {
        if(type.equals(Scene.v().getSootClass("java.lang.Object").getType()))
            return AlwaysMatch.v();


        Type from=value.getSootType();
        Type to=type;
        if(from instanceof PrimType || to instanceof PrimType) {
            if(from.equals(to)) return AlwaysMatch.v();

            if (!(from instanceof PrimType && to instanceof PrimType))
                return NeverMatch.v(); // only one of them primitive

            // FIXME: check that the Java widening primitive conversions are
            // the right thing to do in this context
            // attempts to create a test case crash ajc, which makes things hard

            if (Restructure.JavaTypeInfo.isSimpleWideningConversion(from, to))
                return AlwaysMatch.v();

            return NeverMatch.v();

            /*if(from instanceof ByteType) from=ShortType.v();
            if(from.equals(to)) return AlwaysMatch.v;

            if(from instanceof ShortType || from instanceof CharType)
                from=IntType.v();
            if(from.equals(to)) return AlwaysMatch.v;

            if(from instanceof IntType) from=LongType.v();
            if(from.equals(to)) return AlwaysMatch.v;

            if(from instanceof LongType) from=FloatType.v();
            if(from.equals(to)) return AlwaysMatch.v;

            if(from instanceof FloatType) from=DoubleType.v();
            if(from.equals(to)) return AlwaysMatch.v;
            */

        } else {
            FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();

            if(from instanceof NullType) return NeverMatch.v();

            if(hier.canStoreType(from,to))
                return AlwaysMatch.v();
            // For strict compliance with ajc 1.2.0, we *must* eliminate this much, and
            // anything further we decide we can eliminate (e.g. using a global analysis)
            // must be replaced by an "is not null" check
            // This is because if ajc treats null differently if it
            // eliminates the static type check than if it doesn't.

            if (Restructure.JavaTypeInfo.isForbiddenConversion(from, to))
                return NeverMatch.v();

        }

        Residue res=new CheckType(value,type);

        if(!abc.main.Debug.v().ajc120Compliance) {
            // When not in ajc 1.2.0 compliance mode, we always consider that null
            // is a valid instance of any (reference) type
            // See ajc bug 68603; hopefully newer versions of ajc will do this too
            res=OrResidue.construct(new IsNull(value),res);
        }
        return res;
    }

    public String toString() {
        return "checktype("+value+","+type+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {

        Value v=value.getSootValue();
        Local io=localgen.generateLocal(BooleanType.v(),"checkType");
        Stmt instancetest
            =Jimple.v().newAssignStmt(io,Jimple.v().newInstanceOfExpr(v,type));
        if(abc.main.Debug.v().tagResidueCode)
            instancetest.addTag(new soot.tagkit.StringTag("^^ instanceof check for "+this));
        Expr test;
        if(sense) test=Jimple.v().newEqExpr(io,IntConstant.v(0));
        else test=Jimple.v().newNeExpr(io,IntConstant.v(0));
        Stmt abort=Jimple.v().newIfStmt(test,fail);
        if(wc.getKindTag() == null) {
            wc.setKindTag(InstructionKindTag.ADVICE_ARG_SETUP);
        }
        Tagger.tagStmt(instancetest, wc);
        units.insertAfter(instancetest,begin);
        Tagger.tagStmt(abort, wc);
        units.insertAfter(abort,instancetest);
        return abort;
    }

}

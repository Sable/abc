/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2006 Eric Bodden
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

package abc.weaving.aspectinfo;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.FastHierarchy;
import soot.NullType;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.around.AroundWeaver;

/** Advice specification for around advice.
 *  @author Aske Simon Christensen
 *  @author Eric Bodden
 */
public class AroundAdvice extends AbstractAdviceSpec {
    private AbcType rtype;
    private MethodSig proceed;

    public AroundAdvice(AbcType rtype, MethodSig proceed, Position pos) {
        super(pos);
        this.rtype = rtype;
        this.proceed = proceed;
    }

    public AbcType getReturnType() {
        return rtype;
    }

    /** get the signature of the dummy placeholder method that is called
     *  as a representation of proceed calls inside this around advice.
     */
    public MethodSig getProceedImpl() {
        return proceed;
    }

    public String toString() {
        return rtype+" around";
    }

    private void reportError(String s, ShadowMatch sm) {
        abc.main.Main.v().error_queue.enqueue
            (ErrorInfoFactory.newErrorInfo
             (ErrorInfo.SEMANTIC_ERROR,
              s,
              sm.getContainer(),
              sm.getHost()));
        //Main.v().error_queue.enqueue(
                        //      ErrorInfo.SEMANTIC_ERROR, s);
    }
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
        if (!sm.supportsAround()) {
            // FIXME: should be a multi-position error
            if(ad instanceof AdviceDecl)
                abc.main.Main.v().error_queue.enqueue
                    (ErrorInfoFactory.newErrorInfo
                     (ErrorInfo.WARNING,
                      sm.joinpointName()+" join points do not support around advice, but some advice "
                      +"from "+ad.errorInfo()+" would otherwise apply here",
                      sm.getContainer(),
                      sm.getHost()));
            return NeverMatch.v();
        }
        Type shadowType=sm.getReturningContextValue().getSootType();

        if (shadowType.equals(NullType.v()))
                shadowType=VoidType.v();
        Type adviceType=getReturnType().getSootType();
        if (adviceType.equals(NullType.v())) throw new InternalCompilerError("");

        try {
                checkTypes(shadowType, adviceType);
        } catch (RuntimeException e) {
                reportError(
                            "Invalid application of around advice from "+ad.errorInfo()+" : " +
                                e.getMessage() +
                                        " (shadow type: " + shadowType +
                                        "; advice return type: " + adviceType + ")",sm);
                return NeverMatch.v(); // don't weave if type error
        }

        return AlwaysMatch.v();
    }

        /**
         * @param shadowType
         * @param adviceType
         * @throws SemanticException
         *
         *
         */
    // TODO: verify that this is the desired type check
        private void checkTypes(Type shadowType, Type adviceType) {
                Type objectType=Scene.v().getSootClass("java.lang.Object").getType();
        if (adviceType.equals(objectType)) {
                // object type advice can always be applied
        } else {
                final boolean bVoidAdvice=adviceType.equals(VoidType.v());
                final boolean bVoidShadow=shadowType.equals(VoidType.v());
                if (bVoidAdvice && !bVoidShadow)
                        throw new RuntimeException(
                                        "Can't apply around advice with void return type to a non void shadow");

                if (!bVoidAdvice && bVoidShadow)
                        throw new RuntimeException(
                                        "Can't apply around advice with non-object non-void return type to a void shadow");

                FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();
                if (bVoidAdvice && bVoidShadow) {
                        //
                } else {
                        if (shadowType instanceof PrimType || adviceType instanceof PrimType) {
                                if (!(shadowType instanceof PrimType && adviceType instanceof PrimType))
                                        throw new RuntimeException("Can't convert between primitive type and reference type");

                                if (!Restructure.JavaTypeInfo.isSimpleWideningConversion(adviceType, shadowType))
                                        throw new RuntimeException("Illegal narrowing cast");
                        } else {
                                if (hier.canStoreType(adviceType, shadowType)) {

                                } else
                                        throw new RuntimeException("Advice return type can't be converted");
                        }
                        if (Restructure.JavaTypeInfo.isForbiddenConversion(shadowType, adviceType))
                                throw new RuntimeException("Incompatible types");
                }
        }
        }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
        AroundWeaver.v().doWeave(method.getDeclaringClass(),method,localgen,adviceappl);
    }
}

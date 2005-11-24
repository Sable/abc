/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

import java.util.*;
import polyglot.util.Position;
import soot.*;
import soot.jimple.Jimple;
import soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Synthetic advice to support instantiation of pertarget aspects
 *  @author Ganesh Sittampalam
 */
public class PerTargetSetup extends PerSetupAdvice {

    protected PerTargetSetup(Aspect aspct,Pointcut pc,Position pos) {
        super(new BeforeAdvice(pos),aspct,pc,pos);
    }

    public WeavingContext makeWeavingContext() {
        return new SingleValueWeavingContext();
    }

    public Chain makeAdviceExecutionStmts
        (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

        SingleValueWeavingContext svwc=(SingleValueWeavingContext) wc;
        SootClass aspectclass=getAspect().getInstanceClass().getSootClass();

        Chain c=new HashChain();

        List paramTypes=new ArrayList(1);
        paramTypes.add(Scene.v().getSootClass("java.lang.Object").getType());

        c.addLast(Jimple.v().newInvokeStmt
                  (Jimple.v().newStaticInvokeExpr
                   (Scene.v().makeMethodRef(aspectclass,"abc$perTargetBind",paramTypes,VoidType.v(),true),
                    svwc.value)));
        Tagger.tagChain(c, InstructionKindTag.PEROBJECT_ENTRY);
        if(wc.getShadowTag() != null) {
            Tagger.tagChain(c, wc.getShadowTag());
        }
        if(wc.getSourceTag() != null) {
            Tagger.tagChain(c, wc.getSourceTag());
        }
         
        return c;

    }

    public Residue postResidue(ShadowMatch sm) {
        Type type=Scene.v().getSootClass("java.lang.Object").getType();
        ContextValue targetcv=sm.getTargetContextValue();
        if(targetcv==null) return NeverMatch.v();
        return Bind.construct(targetcv,type,new SingleValueVar(type));
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
        sb.append(prefix+" type: "+spec+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
        sb.append(prefix+" special: pertarget instantiation\n");
    }


}

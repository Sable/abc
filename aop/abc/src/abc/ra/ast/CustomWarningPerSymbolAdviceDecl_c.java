/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Around;
import abc.aspectj.ast.Pointcut;
import abc.tm.ast.PerSymbolAdviceDecl_c;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.TMAdviceDecl;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;

/**
 * A special {@link PerSymbolAdviceDecl_c} which generates customized warnings, based on the waring type.
 *
 * @author Eric Bodden
 * @see CustomWarningPerSymbolAdviceDecl
 */
public class CustomWarningPerSymbolAdviceDecl_c extends PerSymbolAdviceDecl_c
		implements CustomWarningPerSymbolAdviceDecl {

	private final int warningType;

	public CustomWarningPerSymbolAdviceDecl_c(Position pos, Flags flags,
			AdviceSpec spec, List throwTypes, Pointcut pc, Block body,
			String tm_id, SymbolDecl sym, Position tm_pos, int warningType) {
		super(pos, flags, spec, throwTypes, pc, body, tm_id, sym, tm_pos);
		this.warningType = warningType;
	}
	

    /**
     * Mostly copied, just registers a subtype of {@link TMAdviceDecl} which provides special warnings. 
     */
    public void update(GlobalAspectInfo gai, Aspect current_aspect)
    {
        int lastpos = formals().size();
        int jp = -1, jpsp = -1, ejp = -1;
        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;

        // Since the spec is not visited, we copy the (checked)
        // return type node from the advice declaration
        spec.setReturnType(returnType());
        // And the return formal as well
        if (retval != null) {
            spec.setReturnVal(retval);
        }
	
        List<MethodSig> methods = new ArrayList<MethodSig>();
        for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext(); ) {
            CodeInstance ci = (CodeInstance) procs.next();
            if (ci instanceof MethodInstance)
			methods.add(AbcFactory.MethodSig((MethodInstance)ci));
            if (ci instanceof ConstructorInstance)
            methods.add(AbcFactory.MethodSig((ConstructorInstance)ci));
        }

        abc.tm.weaving.aspectinfo.TMAdviceDecl ad =
	        new abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl
	            (spec.makeAIAdviceSpec(),
	            pc.makeAIPointcut(),
	            AbcFactory.MethodSig(this),
	            current_aspect,
	            jp, jpsp, ejp, methods,
	            position(), tm_id, tm_pos, sym.name(), TMAdviceDecl.OTHER) {
        	
        	public String getApplWarning() {
                if (isOther() && super.getApplWarning() != null) {
                	switch(warningType) {
                	case REL_ASPECT:
                		return "Relational aspect is never associated.";
                	case ADVICE:
                		return "Advice doesn't match anywhere.";
                	case NONE:
                		return null;
            		default:
            			return super.getApplWarning();
                	}
                }
                return null;
        	}
        	
        };

        gai.addAdviceDecl(ad);
	
        // don't advise this method or calls to it
        MethodCategory.register(this, MethodCategory.IF_EXPR);
        if (spec instanceof Around) {
            MethodCategory.register(((Around)spec).proceed(),
                                    MethodCategory.PROCEED);
        }
    }

}

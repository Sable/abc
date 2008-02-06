/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import abc.eaj.extension.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class EAJNodeFactory_c extends AJNodeFactory_c
                              implements EAJNodeFactory
{

    public EAJNodeFactory_c() {
        super(new EAJExtFactory_c(), new EAJDelFactory_c());
    }
    
    public EAJNodeFactory_c(EAJExtFactory_c nextExtFactory) {
        super(nextExtFactory, new EAJDelFactory_c());
    }
    
    public EAJNodeFactory_c(EAJExtFactory_c nextExtFactory, EAJDelFactory_c nextDelFactory) {
        super(nextExtFactory, nextDelFactory);
    }

    public PCCast PCCast(Position pos, TypePatternExpr type_pattern)
    {
        PCCast n = new PCCast_c(pos, type_pattern);
        n = (PCCast)n.ext(((EAJExtFactory)extFactory()).extPCCast());
        n = (PCCast)n.del(((EAJDelFactory)delFactory()).delPCCast());
        return n;
    }
    
    public PCThrow PCThrow(Position pos, TypePatternExpr type_pattern)
    {
        PCThrow n = new PCThrow_c(pos, type_pattern);
        n = (PCThrow)n.ext(((EAJExtFactory)extFactory()).extPCThrow());
        n = (PCThrow)n.del(((EAJDelFactory)delFactory()).delPCThrow());
        return n;
    }

    public PCLocalVars PCLocalVars(Position pos,
                                   List varlist,
                                   Pointcut pc)
    {
        PCLocalVars n = new PCLocalVars_c(pos, varlist, pc);
        n = (PCLocalVars)n.ext(((EAJExtFactory)extFactory()).extPCLocalVars());
        n = (PCLocalVars)n.del(((EAJDelFactory)delFactory()).delPCLocalVars());
        return n;
    }

    public GlobalPointcutDecl GlobalPointcutDecl(
                                    Position pos,
                                    ClassnamePatternExpr aspect_pattern,
                                    Pointcut pc)
    {
        GlobalPointcutDecl n = new GlobalPointcutDecl_c(pos, aspect_pattern,pc);
        n = (GlobalPointcutDecl)n.ext(((EAJExtFactory)extFactory()).extGlobalPointcutDecl());
        n = (GlobalPointcutDecl)n.del(((EAJDelFactory)delFactory()).delGlobalPointcutDecl());
        return n;
    }

    public AdviceDecl AdviceDecl(Position pos, Flags flags,
                                 AdviceSpec spec, List throwTypes,
                                 Pointcut pc, Block body)
    {
        EAJAdviceDecl n = new EAJAdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
        n = (EAJAdviceDecl)n.ext(((EAJExtFactory)extFactory()).extEAJAdviceDecl());
        n = (EAJAdviceDecl)n.del(((EAJDelFactory)delFactory()).delEAJAdviceDecl());
        return n;
    }

    public PCCflowDepth PCCflowDepth(Position pos, Local var, Pointcut pc) {
        PCCflowDepth n = new PCCflowDepth_c(pos,pc,var);
        n = (PCCflowDepth)n.ext(((EAJExtFactory)extFactory()).extPCCflowDepth());
        n = (PCCflowDepth)n.del(((EAJDelFactory)delFactory()).delPCCflowDepth());
        return n;
    }

    public PCCflowBelowDepth PCCflowBelowDepth(Position pos, Local var, Pointcut pc) {
        PCCflowBelowDepth n = new PCCflowBelowDepth_c(pos,pc,var);
        n = (PCCflowBelowDepth)n.ext(((EAJExtFactory)extFactory()).extPCCflowBelowDepth());
        n = (PCCflowBelowDepth)n.del(((EAJDelFactory)delFactory()).delPCCflowBelowDepth());
        return n;
    }

    public PCLet PCLet(Position pos, Local var, Expr expr)
    {
        PCLet n = new PCLet_c(pos,var,expr);
        n = (PCLet)n.ext(((EAJExtFactory)extFactory()).extPCLet());
        n = (PCLet)n.del(((EAJDelFactory)delFactory()).delPCLet());
        return n;
    }
    
    public PCContains PCContains(Position pos, Pointcut param) {
        PCContains n = new PCContains_c(pos,param);
        n = (PCContains)n.ext(((EAJExtFactory)extFactory()).extPCContains());
        n = (PCContains)n.del(((EAJDelFactory)delFactory()).delPCContains());
        return n;
    }
    
    public PCArrayGet PCArrayGet(Position pos) {
    	PCArrayGet n = new PCArrayGet_c(pos);
    	n = (PCArrayGet)n.ext(((EAJExtFactory)extFactory()).extPCArrayGet());
    	n = (PCArrayGet)n.del(((EAJDelFactory)delFactory()).delPCArrayGet());
    	return n;
    }

    public PCArraySet PCArraySet(Position pos) {
    	PCArraySet n = new PCArraySet_c(pos);
    	n = (PCArraySet)n.ext(((EAJExtFactory)extFactory()).extPCArraySet());
    	n = (PCArraySet)n.del(((EAJDelFactory)delFactory()).delPCArraySet());
    	return n;
    }

	public abc.eaj.ast.PCLock PCLock(Position pos) {
    	PCLock n = new PCLock_c(pos);
    	n = (PCLock)n.ext(((EAJExtFactory)extFactory()).extPCMonitorEnter());
    	n = (PCLock)n.del(((EAJDelFactory)delFactory()).delPCMonitorEnter());
    	return n;
	}

	public abc.eaj.ast.PCUnlock PCUnlock(Position pos) {
    	PCUnlock n = new PCUnlock_c(pos);
    	n = (PCUnlock)n.ext(((EAJExtFactory)extFactory()).extPCMonitorExit());
    	n = (PCUnlock)n.del(((EAJDelFactory)delFactory()).delPCMonitorExit());
    	return n;
	}

	public abc.eaj.ast.PCMaybeShared PCMaybeShared(Position pos) {
    	PCMaybeShared n = new PCMaybeShared_c(pos);
    	n = (PCMaybeShared)n.ext(((EAJExtFactory)extFactory()).extPCMaybeShared());
    	n = (PCMaybeShared)n.del(((EAJDelFactory)delFactory()).delPCMaybeShared());
    	return n;
	}
}

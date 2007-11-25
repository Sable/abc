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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.AmbTypeOrLocal;
import abc.aspectj.ast.ClassTypeDotId;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.DotDotFormalPattern;
import abc.aspectj.ast.MethodConstructorPattern;
import abc.aspectj.ast.PCBinary;
import abc.aspectj.ast.Pointcut;
import abc.aspectj.ast.SimpleNamePattern;
import abc.aspectj.ast.TypePatternExpr;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMNodeFactory;

/**
 * Declaration of an <i>associate</i> symbol for the translation of a relational advice.
 * Matches on <code>call(* RelationalAspectName.associate(T1,...,Tn))</code>. 
 *
 * @author Eric Bodden
 */
public class AssociateSymbolDecl_c extends SymbolDecl_c implements AssociateSymbolDecl {

	private static final Position POS = Position.compilerGenerated();

	public AssociateSymbolDecl_c(Position pos, String name, String tracematch_name, boolean bindAspectInstanceInReturn, RelAspectDecl container, TMNodeFactory nf) {
		super(pos, name, createKind(nf,tracematch_name,bindAspectInstanceInReturn), createPointcut(container,nf));
	}
	
	/**
	 * Creates a pointcut <code>call(* RelationalAspectName.associate(..))</code>.
	 * @param container the relational aspect owning this symbol
	 * @param nf current node factory
	 * @return the generated pointcut
	 */
	private static Pointcut createPointcut(RelAspectDecl container, TMNodeFactory nf) {
		//return type
		TypePatternExpr tpe = nf.TPEUniversal(POS);

		List<AmbTypeOrLocal> argsRefs = new ArrayList<AmbTypeOrLocal>();
		Iterator i = container.formals().iterator();
		while (i.hasNext()) {
			polyglot.ast.Formal f = (polyglot.ast.Formal) i.next();
			AmbTypeOrLocal n = nf.AmbTypeOrLocal(POS, nf.AmbTypeNode(POS, f.name()));
			argsRefs.add(n);
		}
		
		ClassnamePatternExpr cnpe = nf.CPEName(POS, nf.SimpleNamePattern(POS, container.name()));
		DotDotFormalPattern ddfp = nf.DotDotFormalPattern(POS);
		List<DotDotFormalPattern> ddf = new LinkedList<DotDotFormalPattern>();
		ddf.add(ddfp);
		SimpleNamePattern snp = nf.SimpleNamePattern(POS, "associate");
		//AspectName.associate(..)
		ClassTypeDotId ctdi = nf.ClassTypeDotId(POS, cnpe, snp);
		MethodConstructorPattern mcp = nf.MethodPattern(POS, Collections.EMPTY_LIST, tpe, ctdi, ddf, Collections.EMPTY_LIST);
		// Left pointcut for binary pointcut associate
		Pointcut ascPL = nf.PCCall(POS, mcp);
		// Right pointcut for binary pointcut associate
		Pointcut ascPR = nf.PCArgs(POS, argsRefs);
		// Binary pointcut with && operator for associate
		return nf.PCBinary(POS, ascPL, PCBinary.COND_AND, ascPR);
	}
	
    /**
     * Generates a symbol advice with a custom warning.
     * This is because in the case that the associate-symbol never matches, the warning should be
     * saying that the relational aspect is never associated rather than that the symbol never matches.
     */
    public AdviceDecl generateSymbolAdvice(TMNodeFactory nf, List formals,
            TypeNode voidn, String tm_id, Position tm_pos)
	{
		// Generate AdviceSpec
		AdviceSpec spec = kind.generateAdviceSpec(nf, formals, voidn);
		
		// Generate an empty `throws' list
		List tlist = new LinkedList();
		
		// Generate the TMAdviceDecl
		return ((RANodeFactory) nf).CustomWarningPerSymbolAdviceDecl(position(), Flags.NONE, spec,
		            tlist, pc, body(nf, name, voidn),
		            tm_id, this, tm_pos,CustomWarningPerSymbolAdviceDecl.REL_ASPECT);
	}
	
	/**
	 * Creates an <code>after returning($tmName$stateVarName)</code> or <code>after returning</code> advice kind
	 * @param nf node factory
	 * @param tracematch_name name of the owning tracematch
	 * @param bindAspectInstanceInReturn whether <code>$tmName$stateVarName</code> should be bound
	 * @return
	 */
	private static SymbolKind createKind(TMNodeFactory nf, String tracematch_name, boolean bindAspectInstanceInReturn) {
		if(bindAspectInstanceInReturn)
			return nf.AfterReturningSymbol(POS, nf.Local(POS, stateVariableName(tracematch_name)));
		else 
			return nf.AfterReturningSymbol(POS);
	}

	private static String stateVariableName(String tracematch_name) {
		return TMFromRelTMDecl_c.stateVariableName(tracematch_name);
	}
}

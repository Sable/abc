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
import abc.aspectj.ast.MethodPattern;
import abc.aspectj.ast.PCBinary;
import abc.aspectj.ast.Pointcut;
import abc.aspectj.ast.SimpleNamePattern;
import abc.aspectj.ast.TypePatternExpr;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMNodeFactory;

/**
 * Declaration of an <i>release</i> symbol for the translation of a relational advice.
 * Matches on <code>call(* RelationalAspectName.release(T1,...,Tn))</code>. 
 *
 * @author Eric Bodden
 */
public class ReleaseSymbolDecl_c extends SymbolDecl_c implements ReleaseSymbolDecl {

	private static final Position POS = Position.compilerGenerated();

	public ReleaseSymbolDecl_c(Position pos, String name, String tracematch_name, RelAspectDecl container, TMNodeFactory nf) {
		super(pos, name, createKind(nf), createPointcut(container,nf));
	}
	
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
		
		DotDotFormalPattern ddfp = nf.DotDotFormalPattern(POS);
		List<DotDotFormalPattern> ddf = new LinkedList<DotDotFormalPattern>();
		ddf.add(ddfp);
		ClassnamePatternExpr cnpe = nf.CPEName(POS, nf.SimpleNamePattern(POS, container.name()));
		//release
		SimpleNamePattern snp = nf.SimpleNamePattern(POS, "release");
		ClassTypeDotId ctdi = nf.ClassTypeDotId(POS, cnpe, snp);
		MethodPattern mcp = nf.MethodPattern(POS, Collections.EMPTY_LIST, tpe, ctdi, ddf, Collections.EMPTY_LIST);
		// Left pointcut for binary pointcut release
		Pointcut relPL = nf.PCCall(POS, mcp);
		// Right pointcut for binary pointcut release
		Pointcut relPR = nf.PCArgs(POS, argsRefs);
		// Binary pointcut with && operator for release
		return nf.PCBinary(POS, relPL, PCBinary.COND_AND, relPR);
	}
	
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
		            tm_id, this, tm_pos,CustomWarningPerSymbolAdviceDecl.NONE);
	}

	private static SymbolKind createKind(TMNodeFactory nf) {
		return nf.BeforeSymbol(POS);
	}

}

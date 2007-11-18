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
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.UniqueID;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.After;
import abc.aspectj.ast.AfterReturning;
import abc.aspectj.ast.AfterThrowing;
import abc.aspectj.ast.Around;
import abc.aspectj.ast.Before;
import abc.aspectj.ast.Pointcut;
import abc.tm.ast.Regex;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMModsAndType;
import abc.tm.ast.TMNodeFactory;

/**
 * A relational tracematch equivalent to a relational advice.
 *
 * @author Eric Bodden
 */
public class RelAdviceTMDecl_c extends RelTMDecl_c {

	private static final Position POS = Position.compilerGenerated();
	private final RelAdviceDecl originator;

	@SuppressWarnings("unchecked")
	public RelAdviceTMDecl_c(
			Position pos, 
			AdviceSpec spec, 
			Pointcut pc, 
			List formals, 
			List throwTypes, 
			Block body,
			RelAspectDecl container,
			RelAdviceDecl originator,
			RANodeFactory nf,
			TypeSystem ts
		) {
		super(
			pos,
			body.position(),
			modsAndType(spec,nf,ts),
			UniqueID.newID("relaspecttm"),
			tmFormals(formals, spec),
			throwTypes,
			tmSymbols(spec, pc, formals, container, pos, nf),
			frequentSymbols(spec),
			tmRegex(nf,spec),
			body
		);
		this.originator = originator;		
	}
	
	/**
	 * @param spec
	 * @return
	 */
	private static List frequentSymbols(AdviceSpec spec) {
		List frequentSymbols = new ArrayList();
		frequentSymbols.add("action");
		if(spec instanceof Around) {
			frequentSymbols.add("any$action");
		}
		return frequentSymbols;
	}

	private static TMModsAndType modsAndType(AdviceSpec spec, TMNodeFactory nf, TypeSystem ts) {
		AdviceSpec before_or_around, after = null;
		boolean isAround;
		TypeNode retType;
		if(spec instanceof Around) {
			Around aroundAdvice = (Around) spec;
			retType = aroundAdvice.returnType();
			before_or_around = nf.Around(POS, retType, aroundAdvice.formals());
			isAround = true;
		}
		else {
			//before or after advice
			TypeNode voidn =  nf.CanonicalTypeNode(Position.COMPILER_GENERATED,ts.Void());
			before_or_around = nf.Before(POS,new LinkedList<Formal>(), voidn);
	        after = nf.After(POS,new LinkedList<Formal>(), voidn);
	        retType = voidn;
			isAround = false;
		}
		
		TMModsAndType modsAndType = nf.TMModsAndType(
				Flags.NONE, false, before_or_around, after, isAround, retType);
		
		return modsAndType;
	}

	private static List<Formal> tmFormals(List<Formal> formals, AdviceSpec spec) {
		List<Formal> traceMatchFormals = new ArrayList<Formal>(formals);
		AdviceFormal returnVal = spec.returnVal();
		if(returnVal!=null) traceMatchFormals.add(returnVal);		
		return traceMatchFormals;
	}

	private static List<SymbolDecl> tmSymbols(AdviceSpec spec, Pointcut pc, List<Formal> formals, RelAspectDecl container, Position pos, RANodeFactory nf) {
		List<SymbolDecl> symbols = new LinkedList<SymbolDecl>();
		// symbol kinds
		SymbolKind advK = null;
		if (spec instanceof Around) {
			List<Local> proVars = new LinkedList<Local>();
			//first add the advice formals
			for (Formal relAspectFormal : formals) {
				proVars.add(nf.Local(POS, relAspectFormal.name()));
			}
			if (spec instanceof RelationalAround) {
				//then, if given, add proceed-formals
				RelationalAround relationalAround = (RelationalAround) spec;
				for (String var : relationalAround.proceedVars()) {
					proVars.add(nf.Local(POS, var));
				}
			}
			advK = nf.AroundSymbol(POS, proVars);
		}
		if (spec instanceof Before) {
			advK = nf.BeforeSymbol(POS);
		}
		if (spec instanceof After) {
			advK = nf.AfterSymbol(POS);
		}
		if (spec instanceof AfterReturning) {
			advK = nf.AfterReturningSymbol(POS);
		}
		if (spec instanceof AfterThrowing) {
			advK = nf.AfterThrowingSymbol(POS);
		}
		
		// symbol declaration for original advice
		SymbolDecl advS = nf.AdviceSymbolDeclaration(pos, "action", advK, pc, true);
		// add advice symbol to symbols list
		symbols.add(advS);

		/*
		 * Around-advice need special treatment. The problem is that actually we would like to specify something like this:
		 *   sym action around(): originalPointcut();
		 *   action+ { original body }
		 * The problem with that is that around-symbols (like "action" here) may only occur at the end of a trace.
		 * Hence, we have to generate two symbols and change the pointcut:
		 *   sym action around(): originalPointcut();
		 *   sym any$action before(): originalPointcut();
		 *   any$action* action { original body }
		 */
		if (spec instanceof Around) {
			SymbolKind anySymbolKind = nf.BeforeSymbol(POS);
			// symbol declaration for any$action symbol
			SymbolDecl anyActionSymbolDecl = nf.AdviceSymbolDeclaration(pos, "any$action", anySymbolKind, pc, false);
			// add advice symbol to symbols list
			symbols.add(anyActionSymbolDecl);
		}

		return symbols;
	}

	private static Regex tmRegex(TMNodeFactory nf, AdviceSpec spec) {
		if (spec instanceof Around) {
			/*
			 * Around-advice need special treatment. The problem is that actually we would like to specify something like this:
			 *   sym action around(): originalPointcut();
			 *   action+ { original body }
			 * The problem with that is that around-symbols (like "action" here) may only occur at the end of a trace.
			 * Hence, we have to generate two symbols and change the pointcut:
			 *   sym action around(): originalPointcut();
			 *   sym any$action before(): originalPointcut();
			 *   any$action* action { original body }
			 */
			
			//(any$action)* action
			return nf.RegexConjunction(
					POS,
					nf.RegexStar(
							POS,
							nf.RegexSymbol(POS, "any$action")
					),
					nf.RegexSymbol(POS, "action")
			);
		} else {
			//action+
			return nf.RegexPlus(POS, nf.RegexSymbol(POS, "action"));
		}
	}

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		originator.typeCheck(tc);
		return super.typeCheck(tc);
	}

}

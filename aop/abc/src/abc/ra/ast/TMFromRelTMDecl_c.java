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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.util.Position;
import polyglot.util.TypedList;
import abc.aspectj.ast.Around;
import abc.ra.ExtensionInfo;
import abc.ra.visit.AroundReplacer;
import abc.ra.visit.RegexShuffle;
import abc.ra.visit.SymbolCollector;
import abc.ra.weaving.aspectinfo.RATraceMatch;
import abc.tm.ast.Regex;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMAdviceDecl;
import abc.tm.ast.TMDecl;
import abc.tm.ast.TMDecl_c;
import abc.tm.ast.TMModsAndType;
import abc.tm.ast.TMNodeFactory;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;

/**
 * A tracematch generated from a relational tracematch.
 * This adds associate and release symbols and rewrites the pattern.
 * A regular expression <i>r</i> becomes transformed to <i>associate r</i>.
 * Also, formals of the relational aspect are added and a state variable is generated.
 *
 * @author Eric Bodden
 */
public class TMFromRelTMDecl_c extends TMDecl_c implements TMDecl {

	/** Suffix for internal state variables. */
	public static final String INTERNAL_STATE_VAR_SUFFIX = "$relaspect$internal$state";
	
	private static final Position POS = Position.compilerGenerated();

	public TMFromRelTMDecl_c(Position pos, TMModsAndType mods_and_type, String tracematch_name, List formals,
			List throwTypes, List symbols, List frequent_symbols, Regex regex,
			Block body, RelAspectDecl container, RelTMDecl_c originator, RANodeFactory nf) {
		super(pos,
			body.position(),
			newModsAndType(mods_and_type,nf),
			tracematch_name,
			newFormals(formals,tracematch_name,container,nf),
			throwTypes,
			newSymbols(symbols,tracematch_name,container,nf),
			frequent_symbols,
			newRegex(regex,symbols,nf),
			body);
	}
	
	private static TMModsAndType newModsAndType(TMModsAndType mods_and_type, TMNodeFactory nf) {
		return nf.TMModsAndType(
				mods_and_type.getFlags().clear(ExtensionInfo.RELATIONAL_MODIFIER), //clear "relational" modifier
				mods_and_type.isPerThread(),
				mods_and_type.beforeOrAroundSpec(),
				mods_and_type.afterSpec(),
				mods_and_type.isAround(),
				mods_and_type.getReturnType()
		);
	}

	private static List<SymbolDecl> newSymbols(List symbols, String tracematch_name, RelAspectDecl container, RANodeFactory nf) {
		List<SymbolDecl> newSymbols = new ArrayList<SymbolDecl>(symbols);
		
		for (SymbolDecl symbolDecl : (List<SymbolDecl>)symbols) {
			if(symbolDecl.kind().equals(SymbolKind.AROUND)) {
				SymbolDecl beforeSym = nf.SymbolDecl(
					symbolDecl.position(),
					symbolDecl.name()+AroundReplacer.BEFORE_SYMBOL_SUFFIX,
					nf.BeforeSymbol(symbolDecl.position()),
					symbolDecl.getPointcut()
				);
				newSymbols.add(beforeSym);
			}
		}
		
		newSymbols.add(nf.StartSymbolDecl(container.position(), "start"));
		newSymbols.add(nf.AssociateSymbolDecl(container.position(), "associate", tracematch_name, true, container));
		newSymbols.add(nf.AssociateSymbolDecl(container.position(), "associateAgain", tracematch_name, false, container));
		newSymbols.add(nf.ReleaseSymbolDecl(container.position(), "release", tracematch_name, container));
		
		return newSymbols;
	}

	/**
	 * (start | release) associate (associateAgain* ~ r)+
	 * ... where ~ is the shuffle operator
	 */
	private static Regex newRegex(Regex originalRegex, List symbols, TMNodeFactory nf) {
		//find all around-symbols
		
		Set<String> aroundSymbols = new HashSet<String>();
		for (SymbolDecl symbolDecl : (List<SymbolDecl>)symbols) {
			if(symbolDecl.kind().equals(SymbolKind.AROUND)) {
				aroundSymbols.add(symbolDecl.name());
			}
		}

		//construct a disjunction of all symbols occuring in the original regular expression,
		//replacing around-symbol names by their before-symbol names
		Regex noAroundOriginalRegex = (Regex) originalRegex.visit(new AroundReplacer(aroundSymbols,nf));		
		SymbolCollector symbolCollector = new SymbolCollector();
		noAroundOriginalRegex.visit(symbolCollector);
		Set<String> noAroundOriginalRegexSymbolNames = symbolCollector.getSymbolNames();
		Regex posDisjunction = disjunctionOf(noAroundOriginalRegexSymbolNames,nf);	

		//construct a disjunction of all symbols of the original tracematch not occuring in that tracematch's regex,
		//(i.e. all its skip-symbols)
		Set<String> symbolNames = new HashSet<String>();
		for (SymbolDecl symbol : (List<SymbolDecl>)symbols) {
			symbolNames.add(symbol.name());
		}
		symbolCollector = new SymbolCollector();
		originalRegex.visit(symbolCollector);
		Set<String> originalRegexSymbolNames = symbolCollector.getSymbolNames();
		Set<String> skipSymbols = new HashSet<String>(symbolNames);
		skipSymbols.removeAll(originalRegexSymbolNames);
		Regex negDisjunction = null;
		if(!skipSymbols.isEmpty()) {
			negDisjunction = disjunctionOf(skipSymbols,nf);	
		}

		//create copy of original regex with asssociateAgain* shuffled into it
		Regex aaStar = nf.RegexStar(
				POS,
				nf.RegexSymbol(POS, "associateAgain")
		);		
		Regex shuffeledRegex = (Regex) originalRegex.visit(new RegexShuffle(aaStar,nf));

		//create a copy of that regex with around-symbols replaced
		Regex noAroundShuffledRegex = (Regex) shuffeledRegex.visit(new AroundReplacer(aroundSymbols,nf));
		
		//if there is a negDisjunct (i.e. we had skip-symbols) then fit it in...
		Regex disj = null;
		if(negDisjunction==null) {
			disj = nf.RegexSymbol(POS, "release");
		} else {
			disj = nf.RegexAlternation(
					POS,
					nf.RegexSymbol(POS, "release"),
					negDisjunction
			);
		}
		
		Regex newRegex = nf.RegexConjunction(
			POS,
			nf.RegexAlternation(
				POS,
				nf.RegexSymbol(POS, "start"),
				disj
			),
			nf.RegexConjunction(
				POS,
				nf.RegexStar(
					POS,
					posDisjunction
				),
				nf.RegexConjunction(
					POS,
					nf.RegexSymbol(POS, "associate"),
					nf.RegexConjunction(
						POS,
						nf.RegexStar(
							POS,
							noAroundShuffledRegex
						),
						shuffeledRegex
					)
				)
			)
		);
		return newRegex;
	}

	private static Regex disjunctionOf(Set<String> symbolNames, TMNodeFactory nf) {
		Set<String> copy = new HashSet<String>(symbolNames);
		Iterator<String> iterator = copy.iterator();
		String first = iterator.next();
		iterator.remove();
		Regex regexSymbol = nf.RegexSymbol(Position.COMPILER_GENERATED, first);
		if(copy.isEmpty()) {
			return regexSymbol;
		} else {
			return nf.RegexAlternation(Position.COMPILER_GENERATED, regexSymbol, disjunctionOf(copy, nf));
		}
	}

	private static List newFormals(List formals, String tracematch_name, RelAspectDecl container, TMNodeFactory nf) {
		List traceMatchFormals = new ArrayList(formals);
		traceMatchFormals.addAll(container.formals());
		String formalName = stateVariableName(tracematch_name);
		polyglot.ast.Formal state = nf.Formal(POS, Flags.NONE, nf.AmbTypeNode(POS, container.name()), formalName);
		traceMatchFormals.add(state);
		traceMatchFormals = TypedList.copyAndCheck(traceMatchFormals, Formal.class, true);
		return traceMatchFormals;
	}

	/**
	 * Generates the unique state variable name for a tracematch.
	 */
	public static String stateVariableName(String tracematch_name) {
		return tracematch_name + INTERNAL_STATE_VAR_SUFFIX;
	}
	
	/**
	 * Mostly copied from {@link TMDecl}{@link #update(GlobalAspectInfo, Aspect)}.
	 * Just returns a {@link RATraceMatch} instead of a {@link TraceMatch}.
	 */
	public void update(GlobalAspectInfo gai, Aspect current_aspect) {
        //
        // create aspectinfo advice declarations
        //

        int jp_vars = thisJoinPointVariables();

        // list of what the formals will be for the body-advice
        // after the tracematch formals are removed.
        List transformed_formals = bodyAdviceFormals();
        for (int i = formals.size() - jp_vars; i < formals.size(); i++)
            transformed_formals.add(formals.get(i));


        int lastpos = transformed_formals.size();
        int jp = -1, jpsp = -1, ejp = -1;

        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;


        before_around_spec.setReturnType(returnType());
        if (after_spec != null)
            after_spec.setReturnType(returnType());

        List<MethodSig> methods = new ArrayList<MethodSig>();
        for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext(); )
        {
            CodeInstance ci = (CodeInstance) procs.next();

            if (ci instanceof MethodInstance)
                methods.add(AbcFactory.MethodSig((MethodInstance) ci));
            if (ci instanceof ConstructorInstance)
                methods.add(AbcFactory.MethodSig((ConstructorInstance) ci));
        }

        // create a signature for this method after transformation
        // in the backend (i.e. with only around tracematch formals)
        MethodSig sig = AbcFactory.MethodSig(
                            this.formals(transformed_formals));

        if (before_around_pc != null) {
            abc.weaving.aspectinfo.AdviceDecl before_ad =
                new abc.tm.weaving.aspectinfo.TMAdviceDecl
                    (before_around_spec.makeAIAdviceSpec(),
                     before_around_pc.makeAIPointcut(),
                     sig,
                     current_aspect,
                     jp, jpsp, ejp, methods,
                     position(), name(), position(), TMAdviceDecl.BODY);

            gai.addAdviceDecl(before_ad);
        }

        if (after_pc != null) {
            abc.weaving.aspectinfo.AdviceDecl after_ad =
                new abc.tm.weaving.aspectinfo.TMAdviceDecl
                    (after_spec.makeAIAdviceSpec(),
                     after_pc.makeAIPointcut(),
                     sig,
                     current_aspect,
                     jp, jpsp, ejp, methods, position(),
                     tracematch_name, position(), TMAdviceDecl.BODY);

            gai.addAdviceDecl(after_ad);
        }

        MethodCategory.register(sig, MethodCategory.ADVICE_BODY);

        String proceed_name = null;

        if (isAround) {
            MethodDecl proceed = ((Around) before_around_spec).proceed();
            proceed_name = proceed.name();
            MethodCategory.register(proceed, MethodCategory.PROCEED);
        }

        //
        // Create aspectinfo tracematch
        //
        List tm_formals = weavingFormals(formals, true);
        List body_formals = weavingFormals(transformed_formals, false);
        
        // create TraceMatch
//begin of change
        TraceMatch tm =
            new RATraceMatch(tracematch_name, tm_formals, body_formals,
                           regex.makeSM(), isPerThread, orderedSymToVars(),
                           frequent_symbols, sym_to_advice_name,
                           synch_advice, some_advice, proceed_name,
                           current_aspect, position(),stateVariableName(tracematch_name));
//end of change
        ((TMGlobalAspectInfo) gai).addTraceMatch(tm);
	}
	
}

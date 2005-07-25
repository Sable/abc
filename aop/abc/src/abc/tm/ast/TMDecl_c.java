/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.extension.*;
import abc.aspectj.visit.*;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import abc.tm.weaving.aspectinfo.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class TMDecl_c extends AJMethodDecl_c
                              implements TMDecl, ContainsAspectInfo
{
    protected boolean isPerThread;
    protected boolean isAround;
    protected String tracematch_name;
    protected List symbols;
    protected Regex regex;

    // the set of variable names bound for each symbol
    protected Map sym_to_vars;

    // the name of the per-symbol advice method for each symbol
    protected Map sym_to_advice_name;

    // the name of the some() advice method for each kind of some() advice
    protected Map kind_to_advice_name;

    public TMDecl_c(Position pos,
                    Position body_pos,
                    TMModsAndType mods_and_type,
                    String tracematch_name,
                    List formals,
                    List throwTypes,
                    List symbols,
                    Regex regex,
                    Block body)
    {
        super(body_pos, mods_and_type.getFlags(),
                mods_and_type.getReturnType(),
                tracematch_name + "$body",
                formals, throwTypes, body);

        isPerThread = mods_and_type.isPerThread();
        isAround = mods_and_type.isAround();
        this.tracematch_name = tracematch_name;
        this.symbols = symbols;
        this.regex = regex;
        sym_to_vars = new HashMap();
        sym_to_advice_name = new HashMap();
        kind_to_advice_name = new HashMap();
    }

    //
    // visitor handling code
    //
    protected Node reconstruct(Node n, List symbols)
    {
        if (symbols != this.symbols) {
            TMDecl_c new_n = (TMDecl_c) n.copy();
            new_n.symbols = symbols;

            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Node n = super.visitChildren(v);
        List symbols = visitList(this.symbols, v);
        return reconstruct(n, symbols);
    }
 

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        checkAroundSymbols();
        checkBinding();

        return super.typeCheck(tc);
    }

    protected void checkAroundSymbols() throws SemanticException
    {
        Iterator i = symbols.iterator();
        Collection non_final_syms = regex.nonFinalSymbols();
        Collection final_syms = regex.finalSymbols();

        while (i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();

            if (!isAround && sd.kind() == SymbolKind.AROUND)
                throw new SemanticException(
                    "Around symbols may only appear in around tracematches.",
                    sd.position());

            if (sd.kind() == SymbolKind.AROUND
                    && non_final_syms.contains(sd.name()))
                throw new SemanticException(
                    "Around symbol \"" + sd.name() +
                    "\" may match in the middle of a trace.",
                    regex.position());

            if (isAround && sd.kind() != SymbolKind.AROUND
                    && final_syms.contains(sd.name()))
               throw new SemanticException(
                    "Around tracematches must have around final symbols.",
                    sd.position());
        }
    }

    protected void checkBinding() throws SemanticException
    {
        Iterator i = formals.iterator();
        Collection must_bind = mustBind();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (!must_bind.contains(f.name()))
                throw new SemanticException("Formal \"" + f.name() +
                    "\" is not necessarily bound by tracematch.", f.position());
        }

    }

    protected Collection mustBind() throws SemanticException
    {
        // create a map from symbol names to the names of pointcut
        // variables that the corresponding pointcut binds
        Iterator i = symbols.iterator();

        while(i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();
            sym_to_vars.put(sd.name(), sd.binds());
        }

        // return the set of pointcut variables which must be bound.
        return regex.mustBind(sym_to_vars);
    }
    
    protected Map orderedSymToVars() {
    	Map m = new LinkedHashMap();
    	List formalNames = new LinkedList();
    	for(Iterator fns = formals.iterator(); fns.hasNext(); ) {
    		Formal f = (Formal) fns.next();
    		formalNames.add(f.name());
    	}
    	for (Iterator i = symbols.iterator(); i.hasNext(); ) {
    		SymbolDecl sd = (SymbolDecl) i.next();
    		Collection varbinds = (Collection) sym_to_vars.get(sd.name());
    		List forms = new LinkedList(formalNames);
    		if (!sd.getSymbolKind().binds().isEmpty()) {
    			// after returning formal must come last
    			String n = (String) sd.getSymbolKind().binds().iterator().next();
    			forms.remove(n);
    			forms.add(n);
    		}
    		List vs = new LinkedList();
    		for (Iterator fns = forms.iterator(); fns.hasNext(); ) {
    			String name = (String) fns.next();
    			if (varbinds.contains(name))
    				vs.add(name);
    		}
    		// System.out.println("symbol "+sd.name() + " has formals "+ vs);
    		m.put(sd.name(),vs);
    	}
    	return m;
    }

    public List generateImplementationAdvice(TMNodeFactory nf, TypeNode voidn)
    {
        List advice = new LinkedList();
        List closed_pointcuts = new LinkedList();
        Iterator j = symbols.iterator();
	   	    	
        while(j.hasNext()) {
            SymbolDecl sd = (SymbolDecl) j.next();

            AdviceDecl ad = sd.generateSymbolAdvice(nf, formals, voidn,
                                            tracematch_name, position());
            advice.add(ad);
            sym_to_advice_name.put(sd.name(), ad.name());
            closed_pointcuts.add(sd.generateClosedPointcut(nf, formals));
        }

        makeSomeAdvice(nf, advice, closed_pointcuts, voidn);
        makeSynchAdvice(nf,advice,closed_pointcuts,voidn);

        return advice;
    }

    protected void makeSomeAdvice(TMNodeFactory nf, List advice,
                                    List pointcuts, TypeNode voidn)
    {
        Map kind_to_pointcut = new HashMap();
        Map kind_to_a_symbol = new HashMap();

        Iterator syms = symbols.iterator();
        Iterator pcs = pointcuts.iterator();

        while (syms.hasNext()) {
            SymbolDecl sd = (SymbolDecl) syms.next();
            Pointcut pc = (Pointcut) pcs.next();

            if (kind_to_pointcut.containsKey(sd.kind()))
                pc = nf.PCBinary(position(),
                                 (Pointcut) kind_to_pointcut.get(sd.kind()),
                                 PCBinary.COND_OR,
                                 pc);

            kind_to_pointcut.put(sd.kind(), pc);
            kind_to_a_symbol.put(sd.kind(), sd);
        }

        Iterator kinds = kind_to_pointcut.keySet().iterator();
        while (kinds.hasNext()) {
            SymbolDecl sd = (SymbolDecl) kind_to_a_symbol.get(kinds.next());
            Pointcut pc = (Pointcut) kind_to_pointcut.get(sd.kind());

            AdviceDecl ad = sd.generateSomeAdvice(nf, pc, voidn, returnType(),
                                                tracematch_name, position());
            advice.add(ad);
            kind_to_advice_name.put(sd.kind(), ad.name());
        }
    }
    
    protected void makeSynchAdvice(TMNodeFactory nf, List advice,
    		List pointcuts, TypeNode voidn)
    {
        
        Iterator pcs = pointcuts.iterator();
        Pointcut some = (Pointcut) pcs.next();

        while (pcs.hasNext()) {
            Pointcut pc = (Pointcut) pcs.next();

            some = nf.PCBinary(position(),
                                 some,
                                 PCBinary.COND_OR,
                                 pc);
        }
        
        TypeNode objt = nf.CanonicalTypeNode(position,methodInstance().typeSystem().Object());
        Around aroundspec = nf.Around(position,objt,new LinkedList());
        ProceedCall proceed = nf.ProceedCall(position,nf.This(position),new LinkedList());
        Return ret = nf.Return(position,proceed);
        Block block = nf.Block(position,ret);
        TMAdviceDecl aroundsync = nf.TMAdviceDecl(position,
        		                                  Flags.SYNCHRONIZED,
												  aroundspec,
												  new LinkedList(),
												  some,
												  block,
												  tracematch_name + "$synch",
												  position,
												  TMAdviceDecl_c.SYNCH);
        advice.add(aroundsync);
    }
    
    /**
     * create a TraceMatch object in the GlobalAspectInfo structure
     */
    public void update(GlobalAspectInfo gai, Aspect current_aspect)
    {
        // Convert from polyglot formals to weaving Formals
        List wfs = new ArrayList(formals.size());
        Iterator i = formals.iterator();

        while (i.hasNext()) {
            Formal f  = (Formal) i.next();
            wfs.add(new abc.weaving.aspectinfo.Formal(
                            AbcFactory.AbcType(f.type().type()),
                            f.name(),
                            position()));
        }
        
        // create TraceMatch
        TraceMatch tm =
            new TraceMatch(tracematch_name, wfs, regex.makeSM(), orderedSymToVars(),
                            sym_to_advice_name, kind_to_advice_name,
                            current_aspect,position());

        ((TMGlobalAspectInfo) gai).addTraceMatch(tm);
    }
}

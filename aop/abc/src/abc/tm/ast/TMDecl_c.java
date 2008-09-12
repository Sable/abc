/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2007 Eric Bodden
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Binary;
import polyglot.ast.Block;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Local;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.ast.TypeNode;
import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.UniqueID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceBody_c;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Around;
import abc.aspectj.ast.PCBinary;
import abc.aspectj.ast.Pointcut;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.main.Main;
import abc.tm.AbcExtension;
import abc.tm.visit.MoveTraceMatchMembers;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class TMDecl_c extends AdviceBody_c implements TMDecl
{
    protected boolean isPerThread;
    protected boolean isAround;
    protected String tracematch_name;
    protected List<SymbolDecl> symbols;
    protected List<String> frequent_symbols;
    protected Regex regex;
    
    protected Set<Set<String>> distinctVariableGroups;

    // the set of variable names bound for each symbol
    protected Map<String, Collection<String>> sym_to_vars;

    // the name of the per-symbol advice method for each symbol
    protected Map<String, String> sym_to_advice_name;

    // the names of the sync() and some() advice methods
    protected String synch_advice;
    protected String some_advice;

    // advice specs and pointcuts for the tracematch body
    // (need two, may have before/after or around)
    protected AdviceSpec before_around_spec;
    protected Pointcut before_around_pc;
    protected AdviceSpec after_spec;
    protected Pointcut after_pc;

    public TMDecl_c(Position pos,
                    Position body_pos,
                    TMModsAndType mods_and_type,
                    String tracematch_name,
                    List<Formal> formals,
                    List throwTypes,
                    List<SymbolDecl> symbols,
                    List<String> frequent_symbols,
                    Regex regex,
                    Block body)
    {
        super(pos, mods_and_type.getFlags(),
                mods_and_type.getReturnType(),
                tracematch_name + "$body",
                formals, throwTypes, body,
                mods_and_type.isAround());

        isPerThread = mods_and_type.isPerThread();
        isAround = mods_and_type.isAround();
        before_around_spec = mods_and_type.beforeOrAroundSpec();
        after_spec = mods_and_type.afterSpec();

        this.tracematch_name = tracematch_name;
        this.symbols = symbols;
        this.frequent_symbols = frequent_symbols;
        this.regex = regex;

        sym_to_vars = new HashMap<String, Collection<String>>();
        sym_to_advice_name = new HashMap<String, String>();
    }

    public TMDecl_c(Position pos,
            Position body_pos,
            TMModsAndType mods_and_type,
            String tracematch_name,
            List<Formal> formals,
            List throwTypes,
            List<SymbolDecl> symbols,
            List<String> frequent_symbols,
            Set<Set<String>> distinctVariableGroups,
            Regex regex,
            Block body) {
    	this(pos, body_pos, mods_and_type, tracematch_name, formals, throwTypes, symbols, frequent_symbols, regex, body);
    	this.distinctVariableGroups = new HashSet<Set<String>>(distinctVariableGroups);
    }
    
    //
    // visitor handling code
    //
    protected Node reconstruct(Node n, List<SymbolDecl> symbols,
                                Pointcut before_around_pc, Pointcut after_pc)
    {
        if (   symbols != this.symbols
            || before_around_pc != this.before_around_pc
            || after_pc != this.after_pc)
        {
            TMDecl_c new_n = (TMDecl_c) n.copy();
            new_n.symbols = symbols;
            new_n.before_around_pc = before_around_pc;
            new_n.after_pc = after_pc;

            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        List<Formal> formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);
    
        Pointcut before_around_pc = null;
        if (this.before_around_pc != null)
            before_around_pc = (Pointcut) visitChild(this.before_around_pc, v);

        Pointcut after_pc = null;
        if (this.after_pc != null)
            after_pc = (Pointcut) visitChild(this.after_pc, v);

        List<SymbolDecl> symbols = visitList(this.symbols, v);
        Block body = (Block) visitChild(this.body, v);

        Node n = super.reconstruct(returnType, formals, throwTypes, body);
        return reconstruct(n, symbols, before_around_pc, after_pc);
    }
 
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
                                                    throws SemanticException
    {
        // Check for duplicate symbol definitions
        Iterator<SymbolDecl> syms = symbols.iterator();
        Set<String> names = new HashSet<String>();

        while (syms.hasNext()) {
            SymbolDecl sd = (SymbolDecl) syms.next();

            if (names.contains(sd.name()))
                throw new SemanticException("Symbol \"" + sd.name() +
                            "\"is already defined.", sd.position());

            names.add(sd.name());
        }

        if (frequent_symbols != null) {
            // Check for undeclared symbol names in the list
            // of frequent symbols
        	Iterator<String> namesIt = frequent_symbols.iterator();

            while (syms.hasNext()) {
                String name = (String) namesIt.next();
                if (!names.contains(name))
                    throw new SemanticException(
                        "There is no symbol called \"" + name +
                        "\" but it is declared frequent.", position());
            }
        }

        return super.disambiguateEnter(ar);
    }

    public Context enterScope(Node child, Context c)
    {
        AJContext ajc = (AJContext) super.enterScope(child, c);
        AJTypeSystem ts = (AJTypeSystem) ajc.typeSystem();

        List<Type> formal_types = aroundTypes(c);

        if (child == body && isAroundAdvice && formal_types != null) {
            List<ClassType> throw_types = new LinkedList<ClassType>();
            throw_types.add(ts.Throwable());

            MethodInstance proceedInstance =
                methodInstance().name("proceed")
                                .flags(flags().Public().Static())
                                .formalTypes(formal_types)
                                .throwTypes(throw_types);

            ajc.addProceed(proceedInstance);
        }

        return ajc;
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushProceedFor(this);

        // if-pointcuts in final symbols should not
        // see all tracematch formals
        visitor.pushFormals(bodyAdviceFormals());

        visitor.pushAdvice(this);
    }

    public MethodDecl proceedDecl(AJNodeFactory nf, AJTypeSystem ts)
    {
        if (isAround) {
            TypeNode tn = (TypeNode) returnType().copy();

            List<Formal> formals = new LinkedList<Formal>();
            List<Type> types = new LinkedList<Type>();
            Iterator<Local> locals = aroundVars().iterator();

            while (locals.hasNext()) {
                Local local = (Local) locals.next();
                Formal f = formalFor(local.name());

                formals.add(f); types.add(f.type().type());
            }

            Return ret;

            if (tn.type() == ts.Void())
                ret = nf.Return(position());
            else {
                Expr dummy = dummyVal(nf, tn.type());
                ret = nf.Return(position(), dummy);
            }

            Block bl = nf.Block(position()).append(ret);
            List thrws = new LinkedList();
            String name = UniqueID.newID("proceed");
            MethodDecl md = nf.MethodDecl(position(),
                                Flags.PUBLIC.set(Flags.FINAL).Static(),
                                tn, name, formals, thrws, bl);

            MethodInstance mi =
                ts.methodInstance(position(),
                                  methodInstance().container(),
                                  Flags.PUBLIC.set(Flags.FINAL).Static(),
                                  tn.type(), name,
                                  new ArrayList<Type>(types),
                                  new ArrayList());

            ((ParsedClassType) methodInstance().container()).addMethod(mi);
            md = md.methodInstance(mi);
            ((Around) before_around_spec).setProceed(md);
            return md;
        }

        return null;
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        checkForEmptyTrace();
        checkAroundSymbols();
        if (isAround)
            checkAroundVars(tc.context());
        checkBinding();
        
        if(distinctVariableGroups != null)
        	for(Set<String> group : distinctVariableGroups)
        		for(String var : group)
        			if(!this.mustBind().contains(var))
        				Main.v().getAbcExtension().reportError(ErrorInfo.SEMANTIC_ERROR, 
        						"Reference to unknown tracematch variable " + var, 
        						this.position());

        if (before_around_pc != null)
            before_around_pc.checkFormals(new LinkedList());
        if (after_pc != null)
            after_pc.checkFormals(new LinkedList());

        return super.typeCheck(tc);
    }

    protected void checkForEmptyTrace() throws SemanticException
    {
        if (regex.matchesEmptyString())
            throw new SemanticException(
                "Regular expression matches the empty trace.",
                regex.position());
    }

    protected List<Local> aroundVars()
    {
        Iterator<SymbolDecl> syms = symbols.iterator();
        SymbolDecl sd = syms.next();

        while (sd.kind() != SymbolKind.AROUND && syms.hasNext())
            sd = (SymbolDecl) syms.next();

        if (sd.kind() != SymbolKind.AROUND)
            return null;

        return sd.aroundVars();
    }

    protected void checkAroundVars(Context c) throws SemanticException
    {
        List<Local> around_vars = aroundVars();

        if (around_vars == null)
            throw new SemanticException(
                    "Around tracematches must contain an around symbol.",
                    position());
                                 

        Iterator<Local> vars = around_vars.iterator();
        Collection<String> names = new HashSet<String>();

        while (vars.hasNext()) {
            Local var = vars.next();

            try {
                c.findLocal(var.name()).type();
            } catch (SemanticException e) {
                throw new SemanticException(
                                "Could not find advice formal \"" +
                                var.name() + "\".", var.position());
            }

            if (names.contains(var.name()))
                throw new SemanticException(
                                "Duplicate advice formal in list of " +
                                "proceed arguments.", var.position());

            names.add(var.name());
        }
    }

    protected List<Type> aroundTypes(Context c)
    {
        List<Local> around_vars = aroundVars();

        if (around_vars == null)
            return null;

        List<Type> types = new ArrayList<Type>();
        Iterator<Local> vars = around_vars.iterator();

        while (vars.hasNext()) {
            Local var = vars.next();

            try {
                types.add(c.findLocal(var.name()).type());
            }
            catch (SemanticException e) {
                // check for this error above
            }
        }

        return types;
    }

    protected void checkAroundSymbols() throws SemanticException
    {
        Iterator<SymbolDecl> i = symbols.iterator();
        Collection<String> non_final_syms = regex.nonFinalSymbols();
        Collection<String> final_syms = regex.finalSymbols();
        boolean seen_around = false;

        while (i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();

            if (sd.kind() == SymbolKind.AROUND) {
                if (seen_around)
                    throw new SemanticException(
                        "Only one around symbol is allowed in a tracematch.",
                        sd.position());
 
                seen_around = true;
            }

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
        Iterator<Formal> i = formals.iterator();
        Collection<String> must_bind = mustBind();

        while (i.hasNext()) {
            Formal f = i.next();
            if (!must_bind.contains(f.name()))
                throw new SemanticException("Formal \"" + f.name() +
                    "\" is not necessarily bound by tracematch.", f.position());
        }

    }

    protected Collection<String> mustBind() throws SemanticException
    {
        // create a map from symbol names to the names of pointcut
        // variables that the corresponding pointcut binds
        Iterator<SymbolDecl> i = symbols.iterator();

        while(i.hasNext()) {
            SymbolDecl sd = i.next();
            sym_to_vars.put(sd.name(), sd.binds());
        }

        // return the set of pointcut variables which must be bound.
        return regex.mustBind(sym_to_vars);
    }
    
    protected Map<String, Collection<String>> orderedSymToVars() {
    	Map<String, Collection<String>> m = new LinkedHashMap<String, Collection<String>>();
    	List<String> formalNames = new LinkedList<String>();
    	for(Iterator<Formal> fns = formals.iterator(); fns.hasNext(); ) {
    		Formal f = fns.next();
    		formalNames.add(f.name());
    	}
    	for (Iterator<SymbolDecl> i = symbols.iterator(); i.hasNext(); ) {
    		SymbolDecl sd = i.next();
    		Collection<String> varbinds = sym_to_vars.get(sd.name());
    		List<String> forms = new LinkedList<String>(formalNames);
    		if (!sd.getSymbolKind().binds().isEmpty()) {
    			// after returning formal must come last
    			String n = (String) sd.getSymbolKind().binds().iterator().next();
    			forms.remove(n);
    			forms.add(n);
    		}
    		List<String> vs = new LinkedList<String>();
    		for (Iterator<String> fns = forms.iterator(); fns.hasNext(); ) {
    			String name = fns.next();
    			if (varbinds.contains(name))
    				vs.add(name);
    		}
    		m.put(sd.name(),vs);
    	}
    	return m;
    }

    public List<AdviceDecl> generateImplementationAdvice(TMNodeFactory nf, TypeNode voidn,
                                             MoveTraceMatchMembers visitor)
    {
        List<AdviceDecl> advice = new LinkedList<AdviceDecl>();
        Collection<String> final_syms = regex.finalSymbols();
        Pointcut before = null;
        Pointcut after = null;

        Iterator<SymbolDecl> j = symbols.iterator();

        while(j.hasNext()) {
            SymbolDecl sd = j.next();

            makeSymbolAdvice(nf, advice, sd, voidn);
            Pointcut pc = sd.generateClosedPointcut(nf, formals);
            
            // Take into account distinct declarations.
            // If the pointcut binds two or more symbols in a particular distinct-values
            // set, then add if() pointcuts stipulating the correct inequalities.
            if(distinctVariableGroups != null) {
            	for(Set<String> group : distinctVariableGroups) {
            		Set<String> distinctBound = new HashSet<String>(group);
            		distinctBound.retainAll(sd.binds());
            		if(distinctBound.size() < 2) continue;
            		pc = orPC(nf, pc, nf.PCIf(pc.position(), inequalitiesBetween(distinctBound, nf)));
            	}
            }

            if (sd.kind() == SymbolKind.AFTER) {
                after = orPC(nf, after, pc);

                if (final_syms.contains(sd.name()))
                    after_pc = orPC(nf, after_pc, pc);
            } else {
                before = orPC(nf, before, pc);

                if (final_syms.contains(sd.name())) {
                    if (isAround)
                        before_around_pc = sd.getPointcut();
                    else
                        before_around_pc = orPC(nf, before_around_pc, pc);
                }
            }
        }

        makeEventAdvice(nf, advice, before, after, voidn,
                        "synch()", TMAdviceDecl.SYNCH);
        makeEventAdvice(nf, advice, before, after, voidn,
                        "some()", TMAdviceDecl.SOME);

        return advice;
    }

	protected void makeSymbolAdvice(TMNodeFactory nf, List<AdviceDecl> advice,
                                    SymbolDecl sd, TypeNode voidn)
    {
        AdviceDecl ad = sd.generateSymbolAdvice(nf, formals, voidn,
                                            tracematch_name, position());
        advice.add(ad);
        sym_to_advice_name.put(sd.name(), ad.name());
    }

    protected Pointcut orPC(TMNodeFactory nf, Pointcut orig, Pointcut other)
    {
        if (orig == null)
            return other;

        return nf.PCBinary(Position.COMPILER_GENERATED, orig,
                            PCBinary.COND_OR, other);
    }
    
    protected Expr inequalitiesBetween(Set<String> distinctBound, TMNodeFactory nf) {
    	Position cg = Position.COMPILER_GENERATED;
		Expr result = nf.BooleanLit(cg, true);
		ArrayList<String> todo = new ArrayList<String>(distinctBound);
		for(int i = 0; i < todo.size(); i++) {
			for(int j = i+1; j < todo.size(); j++) {
				result = nf.Binary(cg, 
						result, 
						Binary.COND_AND, 
						nf.Binary(cg, 
								nf.AmbExpr(cg, todo.get(i)), 
								Binary.NE, 
								nf.AmbExpr(cg, todo.get(j))));
			}
		}
		return result;
	}

    protected void makeEventAdvice(TMNodeFactory nf, List<AdviceDecl> advice,
                                   Pointcut before, Pointcut after,
                                   TypeNode voidn, String debug_msg, int kind)
    {
        SymbolDecl sd = (SymbolDecl) symbols.get(0);

        AdviceSpec before_spec = nf.Before(Position.COMPILER_GENERATED, 
                                            new LinkedList(), voidn);
        AdviceSpec after_spec = nf.After(Position.COMPILER_GENERATED,
                                            new LinkedList(), voidn);

        AdviceDecl ad = nf.PerEventAdviceDecl(Position.COMPILER_GENERATED,
                                Flags.NONE, before_spec, before,
                                after_spec, after,
                                sd.body(nf, debug_msg, voidn),
                                tracematch_name, position(), kind);
        advice.add(ad);

        if (kind == TMAdviceDecl.SYNCH)
            synch_advice = ad.name();
        else
            some_advice = ad.name();
    }
 
    protected Formal formalFor(String name)
    {
        Iterator<Formal> i = formals().iterator();

        while (i.hasNext()) {
            Formal f = i.next();
            if (f.name().equals(name))
                return f;
        }

        throw new InternalCompilerError("Can't find " + name);
    }

    protected List<Formal> bodyAdviceFormals()
    {
        if (!isAround)
            return new ArrayList<Formal>();

        List<Formal> body_advice_formals = new ArrayList<Formal>();
        Iterator<Local> around_vars = aroundVars().iterator();

        while (around_vars.hasNext()) {
            Local var = around_vars.next();

            body_advice_formals.add(formalFor(var.name()));
        }

        return body_advice_formals;
    }

    protected int thisJoinPointVariables()
    {
        int count = 0;

        if (hasEnclosingJoinPointStaticPart) count++;
        if (hasJoinPoint) count++;
        if (hasJoinPointStaticPart) count++;

        return count;
    }

    /**
     * convert from polyglot formals to weaving formals
     */
    protected List<abc.weaving.aspectinfo.Formal> weavingFormals(List<Formal> formals, boolean remove_jp_vars)
    {
        int remove = remove_jp_vars ? thisJoinPointVariables() : 0;

        List<abc.weaving.aspectinfo.Formal> wfs = new ArrayList<abc.weaving.aspectinfo.Formal>(formals.size());

        for (int i = 0; i < formals.size() - remove; i++) {
            Formal f  = formals.get(i);

            wfs.add(new abc.weaving.aspectinfo.Formal(
                            AbcFactory.AbcType(f.type().type()),
                            f.name(),
                            position()));
        }

        return wfs;
    }

    /**
     * create a TraceMatch object in the GlobalAspectInfo structure
     */
    public void update(GlobalAspectInfo gai, Aspect current_aspect)
    {
        //
        // create aspectinfo advice declarations
        //

        int jp_vars = thisJoinPointVariables();

        // list of what the formals will be for the body-advice
        // after the tracematch formals are removed.
        List<Formal> transformed_formals = bodyAdviceFormals();
        for (int i = formals.size() - jp_vars; i < formals.size(); i++)
            transformed_formals.add((Formal)formals.get(i));


        int lastpos = transformed_formals.size();
        int jp = -1, jpsp = -1, ejp = -1;

        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;


        before_around_spec.setReturnType(returnType());
        if (after_spec != null)
            after_spec.setReturnType(returnType());

        List<MethodSig> methods = new ArrayList<MethodSig>();
        for (Iterator<CodeInstance> procs = methodsInAdvice.iterator(); procs.hasNext(); )
        {
            CodeInstance ci = procs.next();

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
                     position(), tracematch_name, position(), TMAdviceDecl.BODY);

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
        List<abc.weaving.aspectinfo.Formal> tm_formals = weavingFormals(formals, true);
        List<abc.weaving.aspectinfo.Formal> body_formals = weavingFormals(transformed_formals, false);
        
        // create TraceMatch
        TraceMatch tm =
            new TraceMatch(tracematch_name, tm_formals, body_formals,
                           regex.makeSM(), 
                           isPerThread, orderedSymToVars(),
                           frequent_symbols, sym_to_advice_name,
                           synch_advice, some_advice, proceed_name,
                           current_aspect, position());

        ((TMGlobalAspectInfo) gai).addTraceMatch(tm);
    }

    public String adviceSignature()
    {
        String s = "tracematch(";

        for (Iterator i = formals.iterator(); i.hasNext(); ) {
            Formal t = (Formal) i.next();
            s += t.toString();

            if (i.hasNext()) {
                s += ", ";
            }
        }
        s = s + ")";

        return s;
    }

    public void prettyPrint(CodeWriter c, PrettyPrinter p)
    {
        // FIXME
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs)
    {
        if (body() == null)
            v.visitCFGList(formals(), this);
        else {
            v.visitCFGList(formals(), body().entry());
            v.visitCFG(body(), this);
        }

        return succs;
    }
}

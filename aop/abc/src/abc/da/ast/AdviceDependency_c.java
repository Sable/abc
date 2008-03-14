/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.da.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.Term_c;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.types.AJContext;
import abc.aspectj.visit.ContainsAspectInfo;
import abc.da.HasDAInfo;
import abc.da.types.DAAspectType;
import abc.da.types.DAContext;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.depadviceopt.ds.Bag;
import abc.da.weaving.weaver.depadviceopt.ds.HashBag;
import abc.main.Main;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/**
 * AST node for an advice depencency declaration.
 * @author Eric Bodden
 */
public class AdviceDependency_c extends Term_c implements AdviceDependency, ContainsAspectInfo {

	/** List of strong advice names (and their parameters). */
	protected List<AdviceNameAndParams> strongAdvice;
	/** List of weak advice names (and their parameters). */
	protected List<AdviceNameAndParams> weakAdvice;
	
	public AdviceDependency_c(Position pos, List<AdviceNameAndParams> strongAdvice, List<AdviceNameAndParams> weakAdvice) {
		super(pos);
		this.strongAdvice = strongAdvice;
		this.weakAdvice = weakAdvice;
	}
	
	/** 
	 * Performs the following type checks:
	 * <ul>
	 * <li> Every advice name that is mentioned must refer to an existing {@link AdviceDecl} with that name.
	 * <li> Every advice name must only occur once in this dependency declaration.
	 * <li> There must be at least one strong advice. 
	 * </ul>
	 */
	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		AJContext context = (AJContext) tc.context();
		DAAspectType currentAspectType = (DAAspectType) context.currentAspect();
		Set<AdviceName> aspectAdviceNames = currentAspectType.getAdviceNameToFormals().keySet();
		List<AdviceNameAndParams> allAdviceNames = new LinkedList<AdviceNameAndParams>(strongAdvice);
		allAdviceNames.addAll(weakAdvice);
		// first give an error if we refer to an advice name that does not exist
		for (AdviceNameAndParams adviceName : allAdviceNames) {
			String name = adviceName.getName();
			boolean found = false;
			for (AdviceName aspectAdviceName : aspectAdviceNames) {
				if(name.equals(aspectAdviceName.getName())) {
					found = true;
					break;
				}
			}
			if(!found) {
				throw new SemanticException("Dependent advice does not exist: "+name,adviceName.position());
			}
		}
		
		//check that no advice name occurs twice
		Set<String> occured = new HashSet<String>();
		for (AdviceNameAndParams adviceName : allAdviceNames) {
			if(occured.contains(adviceName.getName())) {
				throw new SemanticException("Advice name '"+adviceName.getName()+
						"' occurs multiple times in dependent advice declaration.",position());
			}
			occured.add(adviceName.getName());
		}
		
		//check for the presence of a strong advice
		if(strongAdvice.isEmpty()) {
			throw new SemanticException("No strong advice present. Need to have at least one strong advice in an advice dependency.",position());
		} 

		return super.typeCheck(tc);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Node typeCheckAdviceParams(ContextVisitor visitor) throws SemanticException {
		//get declared advice names and their formals from the context
		DAContext context = (DAContext) visitor.context();
		DAAspectType currentAspect = (DAAspectType) context.currentAspect();
		Map<AdviceName, List<Formal>> adviceNameToFormals = currentAspect.getAdviceNameToFormals();
		
		//get all advice references in this dependency declaration
		List<AdviceNameAndParams> allAdviceNames = new LinkedList<AdviceNameAndParams>(strongAdvice);
		allAdviceNames.addAll(weakAdvice);
		
		Map<String,Set<Type>> paramNameToPossibleTypes = new HashMap<String, Set<Type>>();
		for (AdviceNameAndParams adviceNameAndParams : allAdviceNames) {
			String referencedAdviceName = adviceNameAndParams.getName();
			for (AdviceName declaredAdviceName : adviceNameToFormals.keySet()) {
				if(declaredAdviceName.getName().equals(referencedAdviceName)) {
					List<Formal> formalsForThisAdviceName =
						adviceNameAndParams.findFormalsForAdviceName(adviceNameToFormals);
					//for all parameters declared in this dependency decl
					List<String> params = adviceNameAndParams.getParams();
					if(formalsForThisAdviceName.size()!=params.size()) {
						String add = formalsForThisAdviceName.size()>params.size() ?
								" Did you maybe forget about a returning/throwing formal?" : "";
						throw new SemanticException("Advice with name '"+referencedAdviceName+"' has "
								+formalsForThisAdviceName.size()+" formal parameter(s), but the dependency " +
										"declaration states "+params.size()+" parameter(s)."+add,
										adviceNameAndParams.position());
					}
					for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
						String paramName = params.get(paramIndex);
						TypeNode paramType = formalsForThisAdviceName.get(paramIndex).type();
						Set<Type> possibleTypes = paramNameToPossibleTypes.get(paramName);
						if(possibleTypes==null) {
							possibleTypes = new HashSet<Type>();
							paramNameToPossibleTypes.put(paramName, possibleTypes);
						}
						possibleTypes.add(paramType.type());
					}
				}
			}
		}
		
		TypeSystem ts = visitor.typeSystem();
		for (Iterator<Map.Entry<String, Set<Type>>> entryIter = paramNameToPossibleTypes.entrySet().iterator(); entryIter.hasNext();) {
			Map.Entry<String, Set<Type>> entry = entryIter.next();
			Set<Type> types = entry.getValue();	
			String var = entry.getKey();
			for (Type type1 : types) {
				for (Type type2 : types) {	
					if(!ts.isCastValid(type1, type2) || !ts.isCastValid(type2, type1)) {
						throw new SemanticException("Incompatible types for variable '"+var+"': "+type1+", "+type2+".",position());
					}
				}
			}			
		}
		
		Bag<String> variableCount = new HashBag<String>();
		for (AdviceNameAndParams adviceNameAndParams : allAdviceNames) {
			variableCount.addAll(adviceNameAndParams.getParams());
		}
		
		for (AdviceNameAndParams adviceNameAndParams : allAdviceNames) {
			if(!adviceNameAndParams.hasInferredParams()) {
				for (String var : adviceNameAndParams.getParams()) {
					if (!var.startsWith(WILDCARD)) {
						if(variableCount.countOf(var)==1) {
							Main.v().getAbcExtension().reportError(ErrorInfo.WARNING, "Variable '"+var+"' only occurs once in this dependency. " +
									"Hence, it will not actually induce any dependency. Consider using the wildcard '*' instead.", adviceNameAndParams.position());
						}
						Set<Type> possibleTypes = paramNameToPossibleTypes.get(var);
						for (Type type : possibleTypes) {
							if(type.isPrimitive()) {
								Main.v().getAbcExtension().reportError(ErrorInfo.WARNING, "Variable '"+var+"' is of primitive type. The dependency analysis will " +
										"not take variables of primitive types into account.", adviceNameAndParams.position());
							}
							//it's enough to look at one parameter because the other ones have to be primitive, too (we already did a type check above)
							break;
						}						
					}
				}
			}
		}
		
		return this;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void update(GlobalAspectInfo gai, Aspect current_aspect) {
		final DAInfo dai = ((HasDAInfo) Main.v().getAbcExtension()).getDependentAdviceInfo();
		
		Map<String,List<String>> strongAdviceNameToVars = new HashMap<String, List<String>>();
		for (AdviceNameAndParams anap : strongAdvice) {
			strongAdviceNameToVars.put(anap.getName(),anap.getParams());
		}
		
		Map<String,List<String>> weakAdviceNameToVars= new HashMap<String, List<String>>();
		for (AdviceNameAndParams anap : weakAdvice) {
			weakAdviceNameToVars.put(anap.getName(),anap.getParams());
		}
		
		abc.da.weaving.aspectinfo.AdviceDependency ad =
			new abc.da.weaving.aspectinfo.AdviceDependency(
					strongAdviceNameToVars,
					weakAdviceNameToVars,
					current_aspect,
					position()
		);
		
		dai.addAdviceDependency(ad);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.begin(0);

        w.write("dependency {");
        w.allowBreak(4);
        
        w.begin(0);
        
        if (! strongAdvice.isEmpty()) {
            w.write("strong ");

            for (Iterator<AdviceNameAndParams> i = strongAdvice.iterator(); i.hasNext(); ) {
                AdviceNameAndParams anap = i.next();
                print(anap, w, pp);
        
                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(0);
                }
            }
            w.write(";");
            w.allowBreak(0);
        }

        if (! weakAdvice.isEmpty()) {
            w.write("weak ");

            for (Iterator<AdviceNameAndParams> i = weakAdvice.iterator(); i.hasNext(); ) {
                AdviceNameAndParams anap = i.next();
                print(anap, w, pp);
        
                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(0);
                }
            }
            w.write(";");
            w.allowBreak(0);
        }

        w.end();

        w.write("}");

        w.end();
	}

	public AdviceDependency_c reconstruct(List<AdviceNameAndParams> strongAdvice, List<AdviceNameAndParams> weakAdvice) {
        if (!strongAdvice.equals(this.strongAdvice) || !weakAdvice.equals(this.weakAdvice)) {
        	AdviceDependency_c n = (AdviceDependency_c) copy();
            n.strongAdvice = strongAdvice;
            n.weakAdvice = weakAdvice;
            return n;
        }
        return this;
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	public Node visitChildren(NodeVisitor v) {
        List<AdviceNameAndParams> strongAdvice= visitList(this.strongAdvice, v);
        List<AdviceNameAndParams> weakAdvice= visitList(this.weakAdvice, v);
        return reconstruct(strongAdvice,weakAdvice);
    }	

	/** 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List acceptCFG(CFGBuilder v, List succs) {
		return succs;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Term entry() {
		return this;
	}

}

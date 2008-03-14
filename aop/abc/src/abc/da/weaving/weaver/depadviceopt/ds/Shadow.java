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
package abc.da.weaving.weaver.depadviceopt.ds;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import polyglot.util.StdErrorQueue;
import soot.Local;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.ondemand.AllocAndContext;
import soot.jimple.spark.ondemand.AllocAndContextSet;
import soot.jimple.toolkits.pointer.FullObjectSet;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.SourceLnPosTag;
import abc.main.Main;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.MethodCallShadowMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ResidueBox;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;

/**
 * Represents a joinpoint shadow in Jimple. Useful for shadow-based optimizations.
 * A shadow's identity is determined by the following values.
 * <ol>
 * <li>The AdviceDecl that caused the shadow to be woven.</li>
 * <li>The shadowId - a unique identifier for the location before or after the shadow was woven.</li>
 * <li>The statement that performs the method call to the advice body.</i>
 * The last item (statement) is necessary because for example an after-advice causes two shadows to
 * be woven: one for normal return and one for exceptional return. Both have the same shadowID
 * and AdviceDecl. 
 * @author Eric Bodden
 */
public class Shadow {
	
	/**
	 * The unique numeric ID of this shadow.
	 * THIS HAS TO BE UNIQUE (see {@link #equals(Object)} and {@link #hashCode()})!
	 */
	protected final int shadowId;
	
	/**
	 * The advice declaration that caused this shadow to be woven. 
	 */
	protected final AdviceDecl adviceDecl;
	
	/**
	 * The method containing this shadow, 
	 */
	protected final SootMethod container;
		
	/**
	 * The position at which this shadow occurs in code.
	 */
	protected Position pos;
	
	/**
	 * The {@link ResidueBox} for this shadow. Can be used to modify the shadow's residue.
	 */
	protected final ResidueBox outerResidueBox;
	
	/**
	 * The list of variables bound by this shadows, in the order of declaration. 
	 */
	protected final List<String> variableOrder;
	
	/**
	 * A mapping from advice formal names to {@link Local}s that bind these formals in code. 
	 */
	protected final Map<String, Local> adviceFormalNameToSootLocal;

	/**
	 * A mapping from advice formal names to points-to sets of the corresponding {@link Local}s.
	 */
	protected final Map<String, PointsToSet> adviceFormalNameToPointsToSet;
	
	/**
	 * The set of formals of this shadow that bind non-{@link RefLikeType}s.
	 */
	protected final Set<String> primitiveFormalNames;

	/**
	 * The invoke statement at which the advice body method for this shadow is called.
	 */
	protected final Stmt stmt;

	/**
	 * If true, this shadow applies to a call statement to a method which resides in a method with the same signature.
	 * In other words, the shadow applies to a delegating call.
	 */
	private final boolean isDelegateCallShadow;
	
	private Shadow(int shadowId, AdviceDecl adviceDecl, SootMethod container, Position pos, Map<String, Local> adviceFormalNameToSootLocal, ResidueBox outerResidueBox, Stmt stmt, boolean isDelegateCallShadow) {
		this.shadowId = shadowId;
		this.adviceDecl = adviceDecl;
		this.container = container;
		this.adviceFormalNameToSootLocal = adviceFormalNameToSootLocal;
		this.pos = pos;
		this.outerResidueBox = outerResidueBox;
		this.stmt = stmt;
		this.isDelegateCallShadow = isDelegateCallShadow;
		this.adviceFormalNameToPointsToSet = new HashMap<String, PointsToSet>();
		List<Formal> formals = adviceDecl.getFormals();
		this.variableOrder = new ArrayList<String>(formals.size());
		this.primitiveFormalNames = new HashSet<String>();
		for (Formal formal : formals) {
			this.variableOrder.add(formal.getName());
			if(!(formal.getType().getSootType() instanceof RefLikeType)) {
				primitiveFormalNames.add(formal.getName());
			}
		}
	}
	
	/**
	 * Returns the aspect that declares the advice that caused this shadow to be woven.
	 */
	public Aspect declaringAspect() {
		return adviceDecl.getAspect();
	}
	
	/**
	 * Disables this shadow by setting its {@link Residue} to {@link NeverMatch}.
	 */
	public void disable() {
		outerResidueBox.setResidue(NeverMatch.v());
	}
	
	/**
	 * Returns <code>true</code> if this shadow's {@link Residue} is anything else but
	 * {@link NeverMatch}.
	 */
	public boolean isEnabled() {
		return !NeverMatch.neverMatches(outerResidueBox.getResidue());
	}
	
	/**
	 * Returns the names of variables bound by this shadow in the order of declaration. 
	 */
	public List<String> variableNames() {
		return variableOrder;
	}
	
	/**
	 * Computes and returns the {@link PointsToSet} for variable
	 * var bound by this shadow. If this shadow does not bind var,
	 * or if it binds var to a primitive value, {@link FullObjectSet} is returned.
	 * @throws IllegalStateException if there is no points-to analysis stored in the {@link Scene}
	 */
	public PointsToSet pointsToSetOf(String var) {
		if(Scene.v().getPointsToAnalysis()==null) {
			throw new IllegalStateException("No points-to analysis found!");
		}		
		if(!variableNames().contains(var) || primitiveFormalNames.contains(var)) {
			return FullObjectSet.v();
		}
		PointsToSet pts = adviceFormalNameToPointsToSet.get(var);
		if(pts==null) {
			Local l = adviceFormalNameToSootLocal.get(var);
			assert l!=null;
			pts = Scene.v().getPointsToAnalysis().reachingObjects(l);
			adviceFormalNameToPointsToSet.put(var, pts);
		}
		return pts;
	}
	
	@Override
	public String toString() {
		String ret = "";
		
		if(pos!=null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bos);		
			StdErrorQueue tmpQueue = new StdErrorQueue(ps,80,"tmp");
			tmpQueue.enqueue(ErrorInfo.WARNING, "", pos);
			tmpQueue.flush();
			String markers = bos.toString();
			markers = markers.replace("Warning --", "");
			markers = markers.replace("\n\n", "");
			ret += markers + "\n\n";
		}

		ret += "shadowId:     " + shadowId + "\n";
		ret += "advice:       " + adviceDecl  + "\n";
		ret += "in method:    " + container  + "\n";
		if(pos!=null) {
		    ret += "position:     " + pos + "\n";
		}
		ret += "variables:    " + adviceFormalNameToSootLocal + "\n";
		ret += "residue:      " + outerResidueBox.getResidue() + "\n";
		
		return ret;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adviceDecl == null) ? 0 : adviceDecl.hashCode());
		result = prime * result + shadowId;
		result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shadow other = (Shadow) obj;
		if (adviceDecl == null) {
			if (other.adviceDecl != null)
				return false;
		} else if (!adviceDecl.equals(other.adviceDecl))
			return false;
		if (shadowId != other.shadowId)
			return false;
		if (stmt == null) {
			if (other.stmt != null)
				return false;
		} else if (!stmt.equals(other.stmt))
			return false;
		return true;
	}

	/**
	 * Creates and returns all active shadows in all weavable methods.
	 */
	public static Set<Shadow> allActiveShadows() {
		return findActiveShadowsInMethod(WeavableMethods.v().getAll());
	}

	/**
	 * Creates and returns all active shadows in all weavable methods reachable from the
	 * program's entry points.
	 */
	public static Set<Shadow> reachableActiveShadows() {
		if(!Scene.v().hasCallGraph()) {
			throw new IllegalStateException("No callgraph present.");
		}
		return findActiveShadowsInMethod(WeavableMethods.v().getReachable(Scene.v().getCallGraph()));
	}

	/**
	 * Creates a set of new {@link Shadow} objects representing all shadows in the given set of methods. 
	 * Shadows, for which the {@link Residue} is {@link NeverMatch} are called inactive. Such inactive shadows are not added.
	 * @param methods any set of (weavable) methods.
	 */
	/**
	 * @param methods
	 * @return
	 */
	protected static Set<Shadow> findActiveShadowsInMethod(Set<SootMethod> methods) {
		GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
		
		/*
		 * First we build a mapping from an advice method to the advice declarations that belong to this advice method.
		 * There can be multiple such advice declarations, e.g. in the case of PerEventAdviceDecls, which have
		 * a before and after pointcut and both trigger the same advice methods but in (aspect info) different advice declarations.  
		 */		
		Map<SootMethod,Set<AdviceDecl>> adviceMethodToAdviceDecls = new HashMap<SootMethod, Set<AdviceDecl>>();
		for (AbstractAdviceDecl aad : (List<AbstractAdviceDecl>)gai.getAdviceDecls()) {
			//only care about "real" advice declarations
			if(aad instanceof AdviceDecl) {
				AdviceDecl ad = (AdviceDecl) aad;
				SootMethod adviceMethod = ad.getImpl().getSootMethod();
				Set<AdviceDecl> adviceDecls = adviceMethodToAdviceDecls.get(adviceMethod);
				if(adviceDecls==null) {
					adviceDecls = new HashSet<AdviceDecl>();
					adviceMethodToAdviceDecls.put(adviceMethod, adviceDecls);
				}
				adviceDecls.add(ad);				
			}			
		}

		Set<Shadow> shadows = new HashSet<Shadow>();
		for (SootMethod m : methods) {

			Map<Integer,Map<AbstractAdviceDecl,AdviceApplication>> shadowIdToAdviceToAdviceApplication = new HashMap<Integer,Map<AbstractAdviceDecl,AdviceApplication>>();
	        MethodAdviceList adviceList = gai.getAdviceList(m);
	        //if there are any advice applications within that method
	        if(adviceList!=null) {
	        	//build a mapping from shadow ID and advice name to the corresponding advice application
		        List<AdviceApplication> applications = adviceList.allAdvice();
		        for (AdviceApplication aa : applications) {	    
		        	Map<AbstractAdviceDecl, AdviceApplication> adviceToAA = shadowIdToAdviceToAdviceApplication.get(aa.shadowmatch.shadowId);
		        	if(adviceToAA==null) {
		        		adviceToAA = new HashMap<AbstractAdviceDecl, AdviceApplication>();
		        		shadowIdToAdviceToAdviceApplication.put(aa.shadowmatch.shadowId, adviceToAA);
		        	}		        	
		        	adviceToAA.put(aa.advice, aa);
		        }
		
		        /*
		         * Walk through all statements in the body, looking for calls to advice methods.
		         * For each such calls, add a shadow for each advice declaration belonging to that call.
		         */
		        for (Unit u : m.getActiveBody().getUnits()) {
					Stmt s = (Stmt) u;
					if(s.containsInvokeExpr()) {
						InvokeExpr ie = s.getInvokeExpr();
						Set<AdviceDecl> adviceDecls = adviceMethodToAdviceDecls.get(ie.getMethod());
						if (adviceDecls!=null) {
							for (AdviceDecl ad : adviceDecls) {
								
								InstructionSourceTag sourceTag = (InstructionSourceTag) s.getTag(InstructionSourceTag.NAME);
								int sourceId = sourceTag.value();
								
								/*
								 * In general, there can be multiple AdviceDecl with the same body advice method.
								 * Therefore we keep the *set* "adviceDecls" above. However, we can always match up
								 * the correct one of these AdviceDecl by comparing on the sourceId. 
								 */								
								if(sourceId==ad.sourceId) {
								
									InstructionShadowTag shadowTag = (InstructionShadowTag) s.getTag(InstructionShadowTag.NAME);
									int shadowId = shadowTag.value();
									
									AdviceApplication aa = shadowIdToAdviceToAdviceApplication.get(shadowId).get(ad);
									if(aa!=null) {
										if(!NeverMatch.neverMatches(aa.getResidue())) {
											//advice is still active
											
											Map<String,Local> adviceFormalToSootLocal = new HashMap<String, Local>();
											int argIndex = 0;
											for (Formal formal : ad.getFormals()) {
												adviceFormalToSootLocal.put(formal.getName(), (Local)ie.getArg(argIndex));
												argIndex++;
											}

											boolean isDelegateCallShadow = false;
											if(aa.shadowmatch instanceof MethodCallShadowMatch) {
												MethodCallShadowMatch mcsm = (MethodCallShadowMatch) aa.shadowmatch;
												SootMethod calledMethod = mcsm.getMethodRef().resolve();
												if(m.getName().equals(calledMethod.getName())
												&& m.getParameterTypes().equals(calledMethod.getParameterTypes())
												&& new HashSet<SootClass>(m.getExceptions()).equals(new HashSet<SootClass>(calledMethod.getExceptions()))) {
													isDelegateCallShadow = true;
												}
											}
											
											Position pos = extractPosition(aa.shadowmatch.getHost());
											ResidueBox rbox = (ResidueBox) aa.getResidueBoxes().get(0); 
											Shadow shadow = new Shadow(
													shadowId,
													ad,
													m,
													pos,
													adviceFormalToSootLocal,
													rbox,
													s,
													isDelegateCallShadow
											);
											shadows.add(shadow);
										}
									}
								}
							}
						}						
					}
		        }
			}
		}
		
		return shadows;
	}

	/**
	 * Returns the position at which the shadow was woven.
	 */
	public Position getPosition() {
		return pos;
	}
	
	/**
	 * Tries to extract a souce position form the host on a best-effort basis.
	 * If we fail, we return <code>null</code>.
	 */
	public static Position extractPosition(Host host) {
    	if(host.hasTag("SourceLnPosTag")) {
    		SourceLnPosTag tag = (SourceLnPosTag) host.getTag("SourceLnPosTag");
    		String fileName = "";
    		if(tag instanceof SourceLnNamePosTag) {
				SourceLnNamePosTag nameTag = (SourceLnNamePosTag) tag;
    			fileName = nameTag.getFileName();
    		}    		
    		return new Position(fileName,tag.startLn(),tag.startPos(),tag.endLn(),tag.endPos());
    	} else if(host.hasTag("LineNumberTag")) {
    		LineNumberTag tag = (LineNumberTag) host.getTag("LineNumberTag");
    		return new Position("",tag.getLineNumber());
    	} else {
    		return null;
    	}
    }

	/**
	 * Returns the advice declaration that caused this shadow to be woven.
	 */
	public AdviceDecl getAdviceDecl() {
		return adviceDecl;
	}

	/**
	 * Returns the method containing this shadow.
	 */
	public SootMethod getContainer() {
		return container;
	}

	/**
	 * Returns all {@link Local} bound by this shadow.
	 */
	public Collection<Local> getBoundSootLocals() {
		return adviceFormalNameToSootLocal.values();
	}    
	
	/**
	 * Returns the set of names of all advice formals bound by this shadows.
	 */
	public Set<String> getAdviceFormalNames() {
		return adviceFormalNameToSootLocal.keySet();
	}
	
	/**
	 * Returns the {@link Local} that was used during weaving to assign the
	 * value of the given formal.
	 * @param formal one of the names returned by {@link #getAdviceFormalNames()}
	 */
	public Local getSootLocalForAdviceFormalName(String formal) {
		assert adviceFormalNameToSootLocal.keySet().contains(formal);
		return adviceFormalNameToSootLocal.get(formal);
	}
	
	/**
	 * Returns an unmodifyable mapping from advice formal names to their respective {@link Local}.
	 */
	public Map<String,Local> getAdviceFormalToSootLocal() {
		return Collections.unmodifiableMap(adviceFormalNameToSootLocal);
	}
	
	/**
	 * Returns the statement that is used to invoke the advice body at this shadow.
	 */
	public Stmt getAdviceBodyInvokeStmt() {
		return stmt;
	}

	/**
	 * Returns the shadow ID for this shadow.
	 * Note that this is <b>NOT</b> a unique identifier for this shadow!
	 */
	public int getID() {
		return shadowId;
	}
	
	/**
	 * Returns true if var is a formal variable that is bound to a primitive value.
	 */
	public boolean isPrimitiveFormal(String var) {
		return primitiveFormalNames.contains(var);
	}
	
	/**
	 * Uses a very crude heuristic to tell whether any of the points-to sets of this shadow
	 * may be imprecise due to dynamic class loading: If toString() of the set contains
	 * DEFAULT_CLASS_LOADER, we return true.
	 */
	public boolean pointsToSetsSufferFromDynamicLoading() {
		//assure that points-to sets exist
		for (String  var : variableOrder) {
			pointsToSetOf(var);
		}		
		return adviceFormalNameToPointsToSet.values().toString().contains("DEFAULT_CLASS_LOADER");
	}
	
	/**
	 * Returns true if at least one points-to set for this shadow has no context information.
	 */
	public boolean notAllPointsToSetsContextSensitive() {
		//assure that points-to sets exist
		for (String  var : variableOrder) {
			pointsToSetOf(var);
		}		
		for (PointsToSet pts : adviceFormalNameToPointsToSet.values()) {
			if(pts instanceof AllocAndContextSet) {
				AllocAndContextSet allocAndContextSet = (AllocAndContextSet) pts;
				for (AllocAndContext allocAndContext : allocAndContextSet) {
					if(allocAndContext.context.isEmpty()) {
						//found an empty context
						return true;
					}
				}
				
			} else {
				//found a context-insensitive points-to set
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this shadow applies to a call statement to a method which resides in a method with the same signature.
	 * In other words, the shadow applies to a delegating call.
	 */
	public boolean isDelegateCallShadow() {
		return isDelegateCallShadow;
	}
}

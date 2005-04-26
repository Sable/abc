/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

package abc.soot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Immediate;
import soot.Local;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtBody;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.options.Options;
import soot.util.Chain;
import abc.weaving.weaver.around.Util;
import abc.weaving.weaver.around.AroundWeaver.LookupStmtTag;

/**
 * @author Sascha Kuzins
 *  
 */
public class SwitchFolder extends BodyTransformer {

	private static void debug(String message)
    { if (abc.main.Debug.v().switchFolder)
        System.err.println("SWF*** " + message);
    }
	
	private static SwitchFolder instance = 
		new SwitchFolder();
	public static void reset() { instance = new SwitchFolder(); }
	
	
	public static SwitchFolder v() { return instance; }
	
	// temporary code.
	// need to create SwitchStmt interface and
	// move common members up the hierarchy
	private Value getKey(Stmt stmt) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			return s.getKey();
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			return s.getKey();
		} else 
			throw new RuntimeException("");
	}
	private Unit getTargetOfKey(Stmt stmt, int key) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			Iterator itT=s.getTargets().iterator();
			for (Iterator it=s.getLookupValues().iterator(); it.hasNext();) {
				IntConstant v=(IntConstant)it.next();
				Stmt target=(Stmt)itT.next();
				if (v.value==key) {
					return target;
				}
			}
			return s.getDefaultTarget();			
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			if (key<s.getLowIndex() || key>s.getHighIndex())
				return s.getDefaultTarget();
			else
				return s.getTarget(key-s.getLowIndex());
		} else 
			throw new RuntimeException("");
	}
	private Set getTargets(Stmt stmt) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			return new HashSet(s.getTargets());	
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			return new HashSet(s.getTargets());
		} else 
			throw new RuntimeException("");
	}
	private Unit getDefaultTarget(Stmt stmt) {
		if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			return s.getDefaultTarget();
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			return s.getDefaultTarget();
		} else 
			throw new RuntimeException("");
	}
	
	protected void internalTransform(Body body, String phaseName, Map options) {


		fold(body, null);

	}

	public void foldWithCheapPropagation(Body b, boolean evaluate) {
		LocalDefs defs=getLocalDefs(b, evaluate);
		fold(b,defs);
	}
	/**
	 * @param body
	 */
	private void fold(Body body, LocalDefs defs) {
		StmtBody stmtBody = (StmtBody)body;
		
		// ConstantPropagatorAndFolder.v().transform(body); // TODO: phase name?
		
		if (Options.v().verbose())
            G.v().out.println("[" + stmtBody.getMethod().getName() +
                               "] Folding switch statements...");

		
		
		Chain units = stmtBody.getUnits();
        List unitList = new ArrayList(units);

        boolean hasFolded=false;
        Iterator stmtIt = unitList.iterator();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
        	if (!units.contains(stmt))
        		continue;
            if (stmt instanceof LookupSwitchStmt ||
            	stmt instanceof TableSwitchStmt) {
                // check for constant-valued conditions
            	//LookupSwitchStmt ls=(LookupSwitchStmt) stmt;
                Value cond = getKey(stmt);
                //Evaluator.getConstantValueOf()
                if (cond instanceof Local && defs!=null) {
                	cond=defs.getLocalConstantValue((Local)cond);
                	if (cond!=null) {
                		
                	} else {
                		continue;
                	}
                } else if (Evaluator.isValueConstantValued(cond) ) {
                    cond = Evaluator.getConstantValueOf(cond);
                } else {
                	continue;
                }
                //if (cond instanceof IntConstant) {
	                int val=((IntConstant) cond).value;
	                
	                Unit target=getTargetOfKey(stmt, val);
	                
	                debug(" Looking at switch statement.");
	                if (stmt.hasTag(LookupStmtTag.name)) {	                	
	                	LookupStmtTag tag=(LookupStmtTag) stmt.getTag(LookupStmtTag.name);
	                	debug(" Found tagged switch statement.");
	                	if (!tag.start)
	                		throw new InternalCompilerError("");
	                	Set targets=getTargets(stmt);
	                	targets.add(getDefaultTarget(stmt));
	                	
	                	for (Iterator it=targets.iterator(); it.hasNext();) {
	                		Stmt s=(Stmt)it.next();	                		
	                		if (s==target)
	                			continue;
	                		if (s==getDefaultTarget(stmt))
	                			continue;
	                		
	                		//debug(" looking at target " + s);
	                		
	                		Stmt r=(Stmt)units.getSuccOf(s);
	                		//debug("   removing " + s);
                			units.remove(s);
                			s=r;
                			if (s==null)
                				throw new InternalCompilerError("");
                			
	                		while(!targets.contains(s)) {
	                			if (s.hasTag(LookupStmtTag.name)) {
	                			//	debug("   found TAG " + s);
	                				LookupStmtTag t=(LookupStmtTag)s.getTag(LookupStmtTag.name);
	                				if (t.ID==tag.ID) {
	                			//		debug("   found matching ID " + t.ID);
	                					break;
	                				}
	                			}
	                			r=(Stmt)units.getSuccOf(s);
	                			//debug("   removing " + s);
	                			units.remove(s);
	                			s=r;
	                			if (s==null)
	                				throw new InternalCompilerError("");
	                		}
	                	}	 
	                	
	                	Stmt s=(Stmt)getDefaultTarget(stmt);
	                	//debug(" looking at default target " + s);
	                	while(true) {
	                		Stmt r=(Stmt)units.getSuccOf(s);
                			//debug("   removing " + s);
                			boolean done=s instanceof ThrowStmt;
                			units.remove(s);
                			s=r;
                			if (s==null)
                				throw new InternalCompilerError("");
                			if (done)
                				break;
	                	}
	                	
	                } 
	                Stmt newStmt =
	                        Jimple.v().newGotoStmt(target);
	                    
	                units.insertAfter(newStmt, stmt);
	                
	                stmt.redirectJumpsToThisTo(newStmt);
	                
	                // remove switch
	                units.remove(stmt);
	                
	                hasFolded=true;
                //}
            }
        }
        if (hasFolded) {
        	Chain traps=body.getTraps();
        	for (Iterator it=new ArrayList(traps).iterator();it.hasNext();) {
        		Trap trap=(Trap)it.next();
        		if (!units.contains(trap.getBeginUnit()) ||
        			!units.contains(trap.getEndUnit()) ||
        			!units.contains(trap.getHandlerUnit())) {
        			if (units.contains(trap.getBeginUnit()) || 
        				units.contains(trap.getBeginUnit()) || 
						units.contains(trap.getBeginUnit())) {
        				throw new InternalCompilerError("");
        			}       			
        			traps.remove(trap);
        		}							
        		
        	}
        }
	}

	
	
	
	public static class LocalDefs {
		LocalDefs(Map locals) {
			this.locals=locals;
		}
		Map locals;
		public Immediate getLocalValue(Local l) {
			return (Immediate)locals.get(l);
		}
		public Constant getLocalConstantValue(Local l) {
			Immediate im=(Immediate)locals.get(l);
			while(im!=null && im instanceof Local) {
				im=(Immediate)locals.get(im);
			}
			return (Constant)im;
		}
	}
	public static void cheapConstantPropagator(Body b, boolean evaluate) {
		LocalDefs defs=getLocalDefs(b, evaluate);
		//Chain statements=b.getUnits();
		for (Iterator it=b.getUseBoxes().iterator(); it.hasNext();) {
			ValueBox box=(ValueBox)it.next();
			Value v=box.getValue();
			if (v instanceof Local) {
				Constant c=defs.getLocalConstantValue((Local)v);
				if (c!=null && box.canContainValue(c)) {
					box.setValue(c);
				}
			}
		}
	}
	public static LocalDefs getLocalDefs(Body b, boolean evaluate) {
		Map locals=new HashMap();
		Chain statements=b.getUnits();
		for (Iterator it=statements.iterator(); it.hasNext();) {
			Stmt s=(Stmt)it.next();
			for (Iterator itB=s.getDefBoxes().iterator(); itB.hasNext();){
				ValueBox box=(ValueBox)itB.next();
				Value val=box.getValue();
				if (val instanceof Local) {
					if(locals.containsKey(val)) {
						locals.put(val, null); // def'd twice..
					} else {
						if (s instanceof DefinitionStmt) {
							DefinitionStmt ds=(DefinitionStmt)s;
							Value v=ds.getRightOp();							
							if (v instanceof Immediate) {
								locals.put(val, ds.getRightOp());
							} else if (evaluate) {
								Value e=Evaluator.getConstantValueOf(v);
								if (e instanceof Constant)
									locals.put(val,e); // (could be null)
								else 
									locals.put(val, null);
							} else {
								locals.put(val, null);
							}
						}
					}
				}
			}
		}
		return new LocalDefs(locals);
	}
	
	/*public static void cheapUnusedCodeRemover(Body b) {
		Chain units=b.getUnits();
		
		int removed;
		do {
			removed=0;
			
			Unit prev=null;
			for (Iterator it=units.iterator();it.hasNext();) {
				Unit s=(Stmt)it.next();
				//System.out.println(" prev:" + prev);
				if (prev!=null) {
					if (!prev.fallsThrough()) {
						if (s.getBoxesPointingToThis().size()==0) {
							//System.out.println("Removing " + s + "Prev:" + prev);
							//prev=(Stmt)units.getPredOf(s);
							units.remove(s);
							removed++;
							it=units.iterator(prev);
							it.next();
							continue;
						}
					}
				}
				prev=s;
			}
		} while (removed>0);
	}*/
	public static void simpleUnusedCodeRemover(Body body) {
		Set reachable=new HashSet();
		findReachableStatements(body, reachable);
		
		Set trapStmts=new HashSet();
		Chain traps=body.getTraps();
		for (Iterator it=traps.iterator();it.hasNext();) {
			Trap trap=(Trap)it.next();
			trapStmts.add(trap.getBeginUnit());
			trapStmts.add(trap.getEndUnit());
			trapStmts.add(trap.getHandlerUnit());
		}	
		Chain statements=body.getUnits();
		List copy=new ArrayList(statements);
		for (Iterator it=copy.iterator();it.hasNext();) {
			Stmt s=(Stmt)it.next();
			if (!reachable.contains(s)) {
				if (!trapStmts.contains(s)) {
					statements.remove(s);
				}
			}
		}
	}
	public static void findReachableStatements(Body body, Set reachable) {
		Chain statements=body.getUnits();
		if (statements.size()==0)
			return;
		
		
		Map trappedStmts=new HashMap();
		Chain traps=body.getTraps();
		for (Iterator it=traps.iterator();it.hasNext();) {
			Trap trap=(Trap)it.next();
			Stmt handler=(Stmt)trap.getHandlerUnit();
			Stmt begin=(Stmt)trap.getBeginUnit();
			Stmt end=(Stmt)trap.getEndUnit();
			for (Iterator itS=statements.iterator(begin);itS.hasNext();) {
				Stmt trapped=(Stmt)itS.next();
				if (trapped==end)
					break;
				if (!trappedStmts.containsKey(trapped))	{				
					trappedStmts.put(trapped, handler);
				} else if (trappedStmts.get(trapped) instanceof Stmt) {
					List handlers=new LinkedList();
					handlers.add(trappedStmts.get(trapped));
					handlers.add(handler);
					trappedStmts.put(trapped, handlers);
				} else {
					List handlers=(List)trappedStmts.get(trapped);
					handlers.add(handler);
				}
			}
		}
		
		Stmt first=(Stmt)statements.getFirst();
		findReachableStatements(body, statements, first, reachable, trappedStmts);
		
			
	}
	public static void findReachableStatements(Body body, Chain statements, Stmt stmt, Set reachable, Map trappedStmts) {
		if (reachable.contains(stmt))
			return;
		reachable.add(stmt);
		
		if (trappedStmts.containsKey(stmt)) {
			Object h=trappedStmts.get(stmt);
			if (h instanceof Stmt) {
				findReachableStatements(body, statements, (Stmt)h, reachable, trappedStmts);
			} else {
				List handlers=(List)h;
				for (Iterator it=handlers.iterator();it.hasNext();) {
					Stmt handler=(Stmt)it.next();
					findReachableStatements(body, statements, handler, reachable, trappedStmts);
				}
			}
		}
		
		if (stmt.fallsThrough()) {
			if (statements.getLast()!=stmt) {
				Stmt s=(Stmt)statements.getSuccOf(stmt);
				findReachableStatements(body, statements, s, reachable, trappedStmts);
			}
		}
		if (stmt instanceof GotoStmt) {
			GotoStmt s=(GotoStmt)stmt;
			findReachableStatements(body, statements, (Stmt)s.getTarget(), reachable, trappedStmts);
		} else if (stmt instanceof IfStmt) {
			IfStmt s=(IfStmt)stmt;
			findReachableStatements(body, statements, (Stmt)s.getTarget(), reachable, trappedStmts);
		} else if (stmt instanceof LookupSwitchStmt) {
			LookupSwitchStmt s=(LookupSwitchStmt)stmt;
			for (Iterator it=s.getTargets().iterator();it.hasNext();){
				Stmt t=(Stmt)it.next();
				findReachableStatements(body, statements, t, reachable, trappedStmts);
			}
		} else if (stmt instanceof TableSwitchStmt) {
			TableSwitchStmt s=(TableSwitchStmt)stmt;
			for (Iterator it=s.getTargets().iterator();it.hasNext();){
				Stmt t=(Stmt)it.next();
				findReachableStatements(body, statements, t, reachable, trappedStmts);
			}
		} else if (stmt instanceof ReturnStmt) {
			
		} else if (stmt instanceof ReturnVoidStmt) {
			
		} else if (stmt instanceof ThrowStmt) {
			
		}
	}
	
}

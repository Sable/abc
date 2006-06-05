/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Neil Ongkingco
 * Copyright (C) 2006 Eric Bodden
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

package abc.eaj.weaving.aspectinfo;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.BodyShadowMatch;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.MethodPosition;
import abc.weaving.matching.NewStmtMethodPosition;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.ShadowType;
import abc.weaving.matching.StmtMethodPosition;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

/**
 * @author Neil Ongkingco
 * @author Eric Bodden
 */
//Would have just extended ShadowPointcut, but Contains needs the entire
//MatchingContext, not just the ShadowMatch
public class Contains extends Pointcut {
    Pointcut param = null;

    public Contains(Position pos, Pointcut param) {
        super(pos);
        this.param = param;
    }

    public void getFreeVars(Set result) {
    }

    public void registerSetupAdvice(Aspect context, Hashtable typeEnv) {
    }

    public Pointcut inline(Hashtable renameEnv, Hashtable typeEnv,
            Aspect context, int cflowdepth) {
        Pointcut newParam = param.inline(renameEnv, typeEnv, context, cflowdepth);
        if (newParam == param) {
            return this;
        }
        return new Contains(getPosition(), newParam);
    }

    public String toString() {
        return "contains(" + param.toString() + ")";
    }

    public Residue matchesAt(MatchingContext mc) throws SemanticException {
        Residue ret = NeverMatch.v();
        
        //only applies to body shadow matches
        if (!(mc.getShadowMatch() instanceof BodyShadowMatch)) {
            return NeverMatch.v();
        }
        
        //taken from AdviceApplication.doShadows and
        // AbcExtension.findMethodShadows
        Chain stmtsChain = mc.getSootMethod().getActiveBody().getUnits();
        Stmt current, next;

        for (current = (Stmt) stmtsChain.getFirst(); current != null; current = next) {

            next = (Stmt) stmtsChain.getSuccOf(current);

            StmtMethodPosition pos = 
                new StmtMethodPosition(mc.getSootMethod(), current);
            NewStmtMethodPosition newPos = 
                new NewStmtMethodPosition(mc.getSootMethod(), current, next);
            
            if (doShadows(mc, pos) == AlwaysMatch.v() ||
                    doShadows(mc, newPos) == AlwaysMatch.v()) {
                ret = AlwaysMatch.v();
                //DEBUG
                //System.out.println(mc.getSootMethod().toString());
                break;
            }
        }
        return ret;
    }

    public Residue doShadows(MatchingContext mc, MethodPosition pos) 
    		throws SemanticException {
        Residue ret = NeverMatch.v();
        for (Iterator i = abc.main.Main.v().getAbcExtension().shadowTypes(); 
        		i.hasNext();) {

            ShadowType st = (ShadowType) i.next();
            ShadowMatch sm;
            
            try {
                sm = st.matchesAt(pos);
            } catch (InternalCompilerError e) {
                throw new InternalCompilerError(
                        e.message(),
                        e.position() == null ? abc.polyglot.util.ErrorInfoFactory
                                .getPosition(pos.getContainer(), pos.getHost())
                                : e.position(), e.getCause());
            } catch (Throwable e) {
                throw new InternalCompilerError(
                        "Error while looking for join point shadow",
                        abc.polyglot.util.ErrorInfoFactory.getPosition(pos
                                .getContainer(), pos.getHost()), e);
            }
            
            Residue currMatch = 
                param.matchesAt(new MatchingContext(mc.getWeavingEnv(),
                        				mc.getSootClass(), 
                        				mc.getSootMethod(),
                        				sm));
            if (currMatch == AlwaysMatch.v()) {
                ret = currMatch;
                break;
            }
        }
        assert (ret == AlwaysMatch.v() || ret == NeverMatch.v()) : 
            "Error: Dynamic match on contains() parameter";
        
        return ret;
    }

}

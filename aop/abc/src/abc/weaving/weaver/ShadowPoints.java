/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.weaver;

import polyglot.util.InternalCompilerError;
import soot.SootMethod;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import abc.weaving.matching.ShadowMatch;

/** A data structure to keep track of the beginning and end points
 * of a pointcut shadow.   Once created, the beginning and end points
 * will alway point to NOP statements.   Weaving will take place just
 * after the beginning NOP and just before the ending NOP.
 * Each abc.weaving.matching.AdviceApplication instance refers to a
 * ShadowPoints instance.   A ShadowPoints instance is shared between all
 * AdviceApplications that apply to a specific pointcut.
 *   @author Laurie Hendren
 *   @date 03-May-04
 *   @author Ondrej Lhotak
 *   @date 04-Oct-04
 *   @author Ganesh Sittampalam
 */

public class ShadowPoints {

    public ShadowPoints inline(ConstructorInliningMap cim) {
        if( cim.inlinee() != container ) throw new InternalCompilerError(
            "inlinee "+cim.inlinee()+" doesn't match container "+container);
        return new ShadowPoints(cim.target(), cim.map(begin), cim.map(end));
    }

    protected final SootMethod container;

    protected final Stmt begin;

    protected final Stmt end;

    /** Should always get references to NopStmts.  For all types of pointcuts
     *  both b and e will be non-null. Even handler pointcuts have an ending
     *  nop, so they can handle BeforeAfterAdvice for cflow etc; but the nop
     *  will initially be right next to the starting nop.
     */
    public ShadowPoints(SootMethod container,Stmt b, Stmt e){
        if (b == null)
            throw new InternalCompilerError("Beginning of shadow point must be non-null");
        if (!(b instanceof NopStmt))
            throw new InternalCompilerError("Beginning of shadow point must be NopStmt");
        if (e == null)
            throw new InternalCompilerError("Ending of shadow point must be non-null");
        if(!(e instanceof NopStmt))
            throw new InternalCompilerError("Ending of shadow point must be NopStmt");
        begin = b;
        end = e;
        //body=container.getActiveBody();
        this.container=container;

        { /// debug
                if (!container.getActiveBody().getUnits().contains(b))
                        throw new InternalCompilerError("Method " + container + " does not contain begin shadow point " + b);
                if (!container.getActiveBody().getUnits().contains(e))
                        throw new InternalCompilerError("Method " + container + " does not contain end shadow point " + e);

        }
    }

    //private final Body body; /// for debugging
    public Stmt getBegin(){
    	//if (body!=container.getActiveBody())
    		//throw new InternalCompilerError("Body has been replaced, invalidating shadow points.");
        return begin;
    }

    public Stmt getEnd(){
    	//if (body!=container.getActiveBody())
    		//throw new InternalCompilerError("Body has been replaced, invalidating shadow points.");
        return end;
    }

    public String toString(){
        return ("ShadowPoint< begin:" + begin + " end:" + end + " >");
    }

    private ShadowMatch shadowmatch=null;
    public void setShadowMatch(ShadowMatch sm) {
        shadowmatch=sm;
    }
    public ShadowMatch getShadowMatch() {
        return shadowmatch;
    }
}

/* abc - The AspectBench Compiler
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

package abc.weaving.matching;

import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.jimple.Stmt;
import abc.weaving.residues.ContextValue;
import abc.weaving.weaver.ConstructorInliningMap;

public class ReroutingShadowMatch extends StmtShadowMatch {

    protected final ShadowMatch delegate;

    protected ReroutingShadowMatch(ShadowMatch bodySM,Stmt stmt) {
        super(bodySM.container, stmt);
        this.delegate = bodySM;
    }

    /**
     * @param cim
     * @return
     * @see abc.weaving.matching.ShadowMatch#inline(abc.weaving.weaver.ConstructorInliningMap)
     */
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ReroutingShadowMatch((BodyShadowMatch)cim.map(delegate),cim.map(stmt));
        cim.add(this, ret);
        return ret;
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#supportsAround()
     */
    public boolean supportsAround() {
        return false;
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "stmt("+delegate.toString()+","+stmt+")";
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#joinpointName()
     */
    public String joinpointName() {
        return delegate.joinpointName();
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#supportsAfter()
     */
    public boolean supportsAfter() {
        return delegate.supportsAfter();
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#supportsBefore()
     */
    public boolean supportsBefore() {
        return delegate.supportsBefore();
    }

    protected SJPInfo makeSJPInfo() {
        return delegate.makeSJPInfo();
    }
   
    /**
     * @return
     * @see abc.weaving.matching.BodyShadowMatch#getArgsContextValues()
     */
    public List getArgsContextValues() {
        return delegate.getArgsContextValues();
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#getReturningContextValue()
     */
    public ContextValue getReturningContextValue() {
        return delegate.getReturningContextValue();
    }

    /**
     * @return
     * @see abc.weaving.matching.BodyShadowMatch#getTargetContextValue()
     */
    public ContextValue getTargetContextValue() {
        return delegate.getTargetContextValue();
    }

    /**
     * @return
     * @see abc.weaving.matching.ShadowMatch#getThisContextValue()
     */
    public ContextValue getThisContextValue() {
        return delegate.getThisContextValue();
    }
    
}

/* abc - The AspectBench Compiler
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
package abc.tm.weaving.aspectinfo;

import java.util.List;

import polyglot.util.Position;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Pointcut;

/**
 * @author Eric Bodden
 */
public class TMPerSymbolAdviceDecl extends TMAdviceDecl {

    protected String symbolId;
    
    /**
     * @param spec
     * @param pc
     * @param impl
     * @param aspct
     * @param jp
     * @param jpsp
     * @param ejp
     * @param methods
     * @param pos
     * @param tm_id
     * @param tm_pos
     * @param sym 
     * @param kind
     */
    public TMPerSymbolAdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspct, int jp, int jpsp, int ejp, List methods, Position pos, String tm_id, Position tm_pos, String symId, int kind) {
        super(spec, pc, impl, aspct, jp, jpsp, ejp, methods, pos, tm_id, tm_pos, kind);
        this.symbolId = symId;
    }
    
    public String getUniqueSymbolId() {
        return (tm_id + "$" + symbolId).intern(); 
    }

}

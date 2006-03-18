/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Neil Ongkingco
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

package abc.eaj.ast;

import java.util.Set;

import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

import abc.aspectj.ast.Pointcut_c;
import abc.eaj.weaving.aspectinfo.Contains;
import abc.weaving.aspectinfo.Pointcut;

/**
 * @author Neil Ongkingco
 *
 */
public class PCContains_c extends Pointcut_c implements PCContains {

    abc.aspectj.ast.Pointcut param = null;
    
    public PCContains_c(Position pos, abc.aspectj.ast.Pointcut param) {
        super(pos);
        this.param = param;
    }
    
    public abc.aspectj.ast.Pointcut getParam() {
        return param;
    }
    
    public boolean isDynamic() {
        return false;
    }
    public Pointcut makeAIPointcut() {
        return new Contains(position, param.makeAIPointcut());
    }
    public Set pcRefs() {
        return param.pcRefs();
    }
    
    PCContains_c reconstruct(abc.aspectj.ast.Pointcut param) {
        PCContains_c ret = this;
        if (param != this.param) {
            ret = new PCContains_c(position, param);
        }
        return ret;
    }
    
    public Node visitChildren(NodeVisitor v) {
        abc.aspectj.ast.Pointcut newParam = 
            (abc.aspectj.ast.Pointcut) visitChild(this.param, v);
        return reconstruct(newParam);
    }
}

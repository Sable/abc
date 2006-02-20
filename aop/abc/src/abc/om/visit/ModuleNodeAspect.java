/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

/*
 * Created on Jul 29, 2005
 *
 */
package abc.om.visit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.aspectj.ast.CPEName;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;

import polyglot.util.Position;

/**
 * Internal representation of a member aspect.
 * @author Neil Ongkingco
 *
 */
public class ModuleNodeAspect extends ModuleNode {
    private PCNode aspectNode; //PCNode for TYPE_ASPECT nodes
    private NamePattern aspectNamePattern; //Name pattern for the aspect
    CPEName cpe;
    
    public ModuleNodeAspect(String name, CPEName cpe, Position pos) {
        this.name = name;
        this.cpe = cpe;
        this.pos = pos;
        
        //get the PCNode representing the aspect by using PCStructure.matchName
        //TODO: Assumes that aspects are always top-level. Needs to be changed
        // if it is no longer the case
        NamePattern namePattern = cpe.getNamePattern();
        PCStructure pcStruct = PCStructure.v();
        Set matches = pcStruct.matchName(namePattern, new PCNode(null, null,
                pcStruct), new HashSet(), new HashSet());
        assert(matches.size() == 1) : "Duplicate aspect name in list";
        Iterator iter = matches.iterator();
        aspectNode = (PCNode) iter.next();
    }
    
    public ClassnamePatternExpr getCPE() {
        return cpe;
    }
    
    public PCNode getAspectNode() {
        return aspectNode;
    }
    
    public boolean isAspect() {
        return true;
    }
    public boolean isClass() {
        return false;
    }
    public boolean isModule() {
        return false;
    }
    public int type() {
        return ModuleNode.TYPE_ASPECT;
    }
}

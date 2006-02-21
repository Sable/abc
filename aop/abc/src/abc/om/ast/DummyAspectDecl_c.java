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
 * Created on Jun 18, 2005
 *
 */
package abc.om.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AspectBody_c;
import abc.aspectj.ast.AspectDecl_c;
import abc.aspectj.ast.IsSingleton_c;
import abc.aspectj.ast.PerClause_c;
import abc.om.AbcExtension;

/**
 * Dummy aspect declaration used by modules. 
 * List contains the module the dummy module represents.
 * 
 * @author Neil Ongkingco
 */
public class DummyAspectDecl_c extends AspectDecl_c {
    List /*ModuleDecl*/ modules;
    
    public DummyAspectDecl_c(Position pos, String moduleName) {
        super(pos, 
                false, 
                Flags.NONE, 
                moduleName + "$DummyAspect", 
                null, 
                new ArrayList(), 
                new IsSingleton_c(pos), 
                new AspectBody_c(pos, new ArrayList())); 
        modules = new ArrayList();
    }
    
    public void addModule(ModuleDecl module) {
        modules.add(module);
    }
    
    public List getModules() {
        return modules;
    }
    
    public DummyAspectDecl_c reconstruct(List modules, Node superRet) {
        if (!CollectionUtil.equals(this.modules, modules)) {
            DummyAspectDecl_c n = (DummyAspectDecl_c) superRet.copy();
            n.modules = modules;
            return n;
        }
        return (DummyAspectDecl_c)superRet;
    }
    
    public Node visitChildren(NodeVisitor v) {
        //AbcExtension.debPrintln("DummyAspectDecl_c.visitChildren " + v.toString());
        Node ret = super.visitChildren(v);
        List newModules = new LinkedList();
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            ModuleDecl moduleDecl = (ModuleDecl) iter.next();
            newModules.add(visitChild(moduleDecl, v));
        }
        
        return reconstruct(newModules, ret);
    }
    
    //Returns true if the aspect name is a dummy aspect
    //Just does a text match
    public static boolean isDummyAspect(String name) {
        String dummyAspectStr = "$DummyAspect";
        if (name.length() <= dummyAspectStr.length()) {
            return false;
        }
        String tail = name.substring(name.length()-dummyAspectStr.length());
        return (tail.compareTo(dummyAspectStr) == 0);
    }
}

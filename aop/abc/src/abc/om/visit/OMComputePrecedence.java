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
 * Created on May 24, 2005
 *
 */
package abc.om.visit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import abc.aspectj.visit.OncePass;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;

/**
 * @author Neil Ongkingco
 *
 */
public class OMComputePrecedence extends OncePass {
    
    private ExtensionInfo ext;

    public OMComputePrecedence(Pass.ID id, Job job, ExtensionInfo ext) {
        super(id);
        this.ext = ext;
    }
    
    /** 
     * Create the precedence relation
     * @see abc.aspectj.visit.OncePass#once()
     */
    protected void once() {
        AbcExtension.debPrintln("openmod compute precedence");
        
        AbcExtension.debPrintln("prec_rel before openmod");
        printPrecRel();
        
        Collection modules = ext.moduleStruct.getModules();
        
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            ModuleNode item = (ModuleNode) iter.next();
            //if has no members, continue
            if (!item.isModule()) {
                continue;
            }
            ModuleNodeModule module = (ModuleNodeModule) item;
            Set /*String*/ prevAspects = new HashSet();
           
            for (Iterator iter2 = module.getMembers().iterator();
            	iter2.hasNext(); ) {
                ModuleNode member = (ModuleNode) iter2.next();
                
                if (member.isModule()) {
                    prevAspects.addAll(((ModuleNodeModule)member).getAspectNames());
                } else if (member.isAspect()) {
                    AbcExtension.debPrintln("Adding ("+member.name() + "," + prevAspects + ")");
                    ext.prec_rel.put(member.name(), new HashSet(prevAspects));
                    prevAspects.add(member.name()); 
                }
            }
        }
        
        AbcExtension.debPrintln("prec_rel after openmod");
        printPrecRel();
    }
    
    private void printPrecRel() {
        //debug: print out precedence relation
        for (Iterator iter = ext.prec_rel.keySet().iterator();
        	iter.hasNext();) {
            String aspectName = (String) iter.next();
            AbcExtension.debPrint(aspectName + " : ");
            Set laterAspects = (Set)ext.prec_rel.get(aspectName);
            
            for (Iterator iter2 = laterAspects.iterator(); iter2.hasNext(); ) {
                String laterAspectName = (String) iter2.next();
                AbcExtension.debPrint(laterAspectName + "; ");
            }
            
            AbcExtension.debPrintln("");
        }
    }

}

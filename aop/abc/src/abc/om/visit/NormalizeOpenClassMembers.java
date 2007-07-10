/* abc - The AspectBench Compiler
 * Copyright (C) 2006
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
package abc.om.visit;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import abc.aspectj.visit.OncePass;
import abc.om.ExtensionInfo;

/**
 * @author Neil Ongkingco
 *
 */
public class NormalizeOpenClassMembers extends OncePass {
    protected ExtensionInfo ext = null;
    
    public NormalizeOpenClassMembers(Pass.ID id, Job job, ExtensionInfo ext) {
        super(id);
        this.ext = ext;
    }

    protected void once() {
        // traverse modules, normalize the open class members
        Collection modules = ext.moduleStruct.getModules();
        
        for (Iterator i = modules.iterator(); i.hasNext();) {
            ModuleNodeModule module = (ModuleNodeModule) i.next();
            
            MSOpenClassMember normOCM = new MSOpenClassMemberBase();
            boolean prevIsContained = false;
            List ancestorList = ext.moduleStruct.getModuleAncestorList(module); 
            for (Iterator j = ancestorList.iterator(); j.hasNext();) {
                ModuleNodeModule currModule = (ModuleNodeModule) j.next();
                if (prevIsContained == false) {
                    normOCM = 
                        new MSOpenClassMemberOr(
                            normOCM, 
                            currModule.getOpenClassMembers());
                } else {
                    normOCM = 
                        new MSOpenClassMemberAnd(
                                normOCM, 
                                currModule.getOpenClassMembers());
                }
                prevIsContained = currModule.isConstrained();
            }
            module.setOpenClassMembers(normOCM);
        }
    }
}

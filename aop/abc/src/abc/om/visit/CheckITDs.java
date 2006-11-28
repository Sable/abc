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

import java.util.Iterator;

import abc.aspectj.types.InterTypeMemberInstance;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.om.ast.OpenModNodeFactory;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * @author Neil Ongkingco
 *
 */
public class CheckITDs extends ContextVisitor {

    protected ExtensionInfo ext = null;
    
    public CheckITDs(Job job, TypeSystem ts, OpenModNodeFactory nf,
            ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }
    
    
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        
        //adapted from MangleNames
        //check classes for InterTypeMemberInstances, and compare with permissions
        //in its owning module
		if (n instanceof ClassDecl) {
		    AbcExtension.debPrintln(AbcExtension.ITD_DEBUG, "check_itd");
		    
			ClassDecl cd = (ClassDecl) n;
			ClassType ct = cd.type();
			PCNode classNode = PCStructure.v().getClass(ct);
			
			ModuleNodeModule ownerModule = 
			    (ModuleNodeModule) 
			    	ext.moduleStruct.getOwner(classNode);
			//if no owner, proceed to other classes
			if (ownerModule == null) {
			    return super.enterCall(parent, n);
			}
			
			//check field itd members
			checkITDFields(ct, classNode, ownerModule);
			
			//check method itd members
			checkMethods(ct, classNode, ownerModule);
			
			//check constructors itd members
			checkConstructors(ct, classNode, ownerModule);
		}
        return super.enterCall(parent, n);
    }


    private void checkConstructors(ClassType ct, 
            PCNode classNode, 
            ModuleNodeModule ownerModule) throws SemanticException {
        //iterate through constructors
        for (Iterator cIt = ct.constructors().iterator(); cIt.hasNext();) {
            ConstructorInstance ci = (ConstructorInstance) cIt.next();
            if (ci instanceof InterTypeMemberInstance) {
                InterTypeMemberInstance itdCi = 
                    (InterTypeMemberInstance) ci;
                //if a friend aspect, ignore constraints
                if (isInSameModuleSet(itdCi.origin(), classNode)) {
                    continue;
                }
                //check with owner module
                AbcExtension.debPrintln(AbcExtension.ITD_DEBUG,
                        "class: " + ct.toString() +
                        " ITD constructor: " + ci.toString());
        	    MSOpenClassMember ocm = ownerModule.getOpenClassMembers();
        	    PCNode aspectPCNode = PCStructure.v().getClass(itdCi.origin());
        	    MSOpenClassContextMethod context = 
        	        new MSOpenClassContextMethod(classNode, aspectPCNode);
        	    if (!ocm.isAllowed(OpenClassFlagSet.METHOD, context)) {
        	        throw new SemanticException(
        	                "Inter-type constructor introduced by aspect " +
        	                itdCi.origin().fullName() +
        	                " to class " + ct.fullName() +
        	                " not allowed by class' owning module " +
        	                ownerModule.name(),
        	                itdCi.position()
        	                );
        	    }
            }
        }
    }


    private void checkMethods(ClassType ct, 
            PCNode classNode, 
            ModuleNodeModule ownerModule) throws SemanticException {
        //iterate through methods
        for (Iterator miIt = ct.methods().iterator(); miIt.hasNext(); ) {
        	MethodInstance mi = (MethodInstance) miIt.next();
        	if (mi instanceof InterTypeMemberInstance) {
        	    InterTypeMemberInstance itdMi = 
        	        (InterTypeMemberInstance) mi;
                //if a friend aspect, ignore constraints
                if (isInSameModuleSet(itdMi.origin(), classNode)) {
                    continue;
                }
        		//check with owner module
        	    AbcExtension.debPrintln(AbcExtension.ITD_DEBUG,
        	            "class: " + ct.toString() +
        	            " ITD method: " + mi.toString());
        	    MSOpenClassMember ocm = ownerModule.getOpenClassMembers();
        	    PCNode aspectPCNode = PCStructure.v().getClass(itdMi.origin());
        	    MSOpenClassContextMethod context = 
        	        new MSOpenClassContextMethod(classNode, aspectPCNode);
        	    if (!ocm.isAllowed(OpenClassFlagSet.METHOD, context)) {
        	        throw new SemanticException(
        	                "Inter-type method introduced by aspect " +
        	                itdMi.origin().fullName() +
        	                " to class " + ct.fullName() +
        	                " not allowed by class' owning module " +
        	                ownerModule.name(),
        	                itdMi.position()
        	                );
        	    }
        	}
        }
    }

    private void checkITDFields(ClassType ct, 
            PCNode classNode, 
            ModuleNodeModule ownerModule) throws SemanticException {
        //iterate through fields
        for (Iterator fIt = ct.fields().iterator(); fIt.hasNext(); ) {
            FieldInstance fi = (FieldInstance) fIt.next();
            if (fi instanceof InterTypeMemberInstance) {
                InterTypeMemberInstance itdFi = 
                    (InterTypeMemberInstance) fi;
                //if a friend aspect, ignore constraints
                if (isInSameModuleSet(itdFi.origin(), classNode)) {
                    continue;
                }
               //check with owner module
                AbcExtension.debPrintln(AbcExtension.ITD_DEBUG,
                        "class: " + ct.toString() +
                        " ITD field: " + fi.toString());
                MSOpenClassMember ocm = ownerModule.getOpenClassMembers();
                PCNode aspectPCNode = PCStructure.v().getClass(itdFi.origin());
                MSOpenClassContextField context = 
                    new MSOpenClassContextField(classNode, aspectPCNode);
                if (!ocm.isAllowed(OpenClassFlagSet.FIELD,context)) {
                    throw new SemanticException(
                            "Inter-type field introduced by aspect " + 
                            itdFi.origin().fullName() +
                            " to class " + ct.fullName() +
                            " not allowed by class' owning module " +
                            ownerModule.name(), 
                            itdFi.position());
                }
            }
        }
    }
    
    private boolean isInSameModuleSet(ClassType aspectType, PCNode classNode) {
        ModuleNodeAspect aspectNode = 
            (ModuleNodeAspect) ext.moduleStruct.getNode(
                    aspectType.fullName(), 
                    ModuleNode.TYPE_ASPECT);
        return ext.moduleStruct.isInSameModuleSet(aspectNode, classNode);
    }
    
}

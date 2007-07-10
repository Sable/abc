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

import java.util.ArrayList;
import java.util.Iterator;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AspectDecl;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.DeclareParents;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.om.ast.OpenModNodeFactory;

/**
 * @author Neil Ongkingco
 *
 */
public class CheckDeclareParents extends ContextVisitor {
    protected ExtensionInfo ext = null;
    
    public CheckDeclareParents(Job job, TypeSystem ts,
            OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    private AspectDecl currAspect = null;
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        // TODO Auto-generated method stub
        if (n instanceof AspectDecl) {
            this.currAspect = (AspectDecl) n;
        }
        if (n instanceof DeclareParents) {
            //assert (currAspect != null) : "Declare parents with no enclosing aspect, possible AST corruption";

            String aspectName = currAspect.type().fullName();
            PCNode aspectPCNode = PCStructure.v().getClass(currAspect.type());
            
            AbcExtension.debPrintln(AbcExtension.PARENT_DEBUG, "check_declare_parents");
            
            DeclareParents dp = (DeclareParents) n;
            ClassnamePatternExpr cpe = dp.pat();
            
            //for all classes that match the declare parents cpe
            for (Iterator i = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
            	i.hasNext();) {
                ClassType ct = (ClassType) i.next();
                PCNode classNode = ext.hierarchy.getClass(ct);
                
                //if does not match the declare parents cpe, continue 
                if (!classNode.isWeavable() || !cpe.matches(classNode)) {
                    continue;
                }
                //if in same module set (i.e. aspect is a friend of the class), don't
                //apply constraints
                ModuleNodeAspect aspectNode = 
                    (ModuleNodeAspect) 
                    	ext.moduleStruct.getNode(aspectName, 
                    	        				ModuleNode.TYPE_ASPECT);
                if (ext.moduleStruct.isInSameModuleSet(aspectNode, classNode)) {
                    continue;
                }
                
            //	get class' owner module
                ModuleNodeModule module = 
                    (ModuleNodeModule) ext.moduleStruct.getOwner(classNode);
            //	if none, continue
                if (module == null) {
                    continue;
                }
            //	else check for each parent
                for (Iterator j = dp.parents().iterator(); j.hasNext(); ) {
	                MSOpenClassMember ocm = module.getOpenClassMembers();
	                TypeNode tn = (TypeNode) j.next();
	                PCNode parentNode = ext.hierarchy.getClass(tn.type().toClass());
	                MSOpenClassContextParent context = 
	                    new MSOpenClassContextParent(classNode, aspectPCNode, parentNode);
	                if (!ocm.isAllowed(OpenClassFlagSet.PARENT, context)) {
           //		if class does not match parent permission, throw an error
	                    throw new SemanticException(
	                            "declare parents not allowed by the parent module " + 
	                            module.name() +
	                            " of class " + classNode.toString(), dp.position());
	                }
                }
            }
        }
        
        return super.enterCall(parent, n);
    }
}

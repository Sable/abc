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
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import abc.aspectj.ast.CPEName;
import abc.om.ExtensionInfo;
import abc.om.visit.*;

import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents the body of the module. Contains class, aspect and signature
 * members
 * @author Neil Ongkingco
 *  
 */
public class ModuleBody_c extends Node_c implements ModuleBody {

    private List /* ModMember */members;

    private List /* SigMember */sigMembers;
    
    private List /*ModMemberOpenClass*/ openClassMembers;

    public ModuleBody_c(Position pos, List members) {
        super(pos);
        this.members = new LinkedList();
        this.sigMembers = new LinkedList();
        this.openClassMembers = new LinkedList();
        for (Iterator iter = members.iterator(); iter.hasNext(); ) {
            Object m = iter.next();
            if (m instanceof ModMember) {
                this.members.add(m);
            } else if (m instanceof SigMember){
                this.sigMembers.add(m);
            } else if (m instanceof OpenClassMember) {
            	this.openClassMembers.add(m);
            }
            else {
                assert(false) : "ModuleBody list contains illegal type: " + m.getClass().toString(); //should never happen
            }
        }
    }

    public List members() {
        return members;
    }

    /*
     * (non-Javadoc)
     * 
     * @see abc.openmod.ast.ModuleBody#sigMembers()
     */
    public List sigMembers() {
        return sigMembers;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.begin(4);
        w.write("/*members*/");
        w.newline();
        for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModMember member = (ModMember) iter.next();
            member.prettyPrint(w, pp);
        }
        w.write("/*signature*/");
        w.newline();
        for (Iterator iter = sigMembers.iterator(); iter.hasNext();) {
            SigMember sigMember = (SigMember) iter.next();
            sigMember.prettyPrint(w, pp);
        }
        w.end();
        w.newline();
        //super.prettyPrint(w, pp);
    }

    public void checkMembers(ModuleDecl module, ExtensionInfo ext)
            throws SemanticException {
        // Checks the members of the module
        for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModMember m = (ModMember) iter.next();
            if (m instanceof ModMemberAspect) {
                checkMemberAspect(module, (ModMemberAspect) m, ext);
            } else if (m instanceof ModMemberClass) {
                checkMemberClass(module, (ModMemberClass) m, ext);
            } else if (m instanceof ModMemberModule) {
                checkMemberModule(module, (ModMemberModule) m, ext);
            }
        }
    }
    
    public void checkSigMembers(ModuleDecl module, ExtensionInfo ext) {
        //checks the signature members of this module
        for (Iterator iter = sigMembers.iterator(); iter.hasNext();) {
            SigMember sigMember = (SigMember) iter.next();
            checkSigMember(module, sigMember, ext);
        }
    }

    /**
     * Checks if the included module exists, and if it is not already a member
     * of another module
     */
    public void checkMemberModule(ModuleDecl module, ModMemberModule member,
            ExtensionInfo ext) throws SemanticException {
        // Check if the module exists
        ModuleNodeModule parentn = 
            	(ModuleNodeModule)ext.moduleStruct.getNode(module.name(),
            	        	ModuleNode.TYPE_MODULE);
        assert(parentn != null);
        ModuleNodeModule n = 
            	(ModuleNodeModule)ext.moduleStruct.getNode(member.name(),
            	        ModuleNode.TYPE_MODULE);
        if (n == null) {
            throw new SemanticException("Module does not exist", member
                    .position());
        }
        
        //check if the module is a root module
        if (n.isRoot()) {
            throw new SemanticException("Root modules cannot be included in other modules", member
                    .position());
        }
        
        // Check if module already belongs to another module
        if (n.getParent() != null) {
            throw new SemanticException("Module already a member of "
                    + n.getParent().name(), member.position());
        }

        //set the members parent to this module and add the node the parent
        ext.moduleStruct.addMember(parentn.name(), n);
        
        //set the constrained flag
        n.setIsConstrained(member.isConstrained());
    }

    public void checkMemberClass(ModuleDecl module, ModMemberClass member,
            ExtensionInfo ext) throws SemanticException {
        // add the ModuleNodes that represent the expression
        ModuleNode n = ext.moduleStruct.addClassNode(module.name(), member.getCPE(), member.position());
        assert(n != null);
        ext.moduleStruct.addMember(module.name(), n);
    }

    public void checkMemberAspect(ModuleDecl module, ModMemberAspect member,
            ExtensionInfo ext) throws SemanticException {
        // Check if the aspect exists
        if (!ext.aspect_names.contains(member.name())) {
            throw new SemanticException("Aspect does not exist", member
                    .position());
        }
        //check if the aspect already belongs to another module
        ModuleNode owner = ext.moduleStruct.getOwner(member.name(),
                ModuleNode.TYPE_ASPECT);
        if (owner != null) {
            throw new SemanticException("Aspect already included in module "
                    + owner.name(), member.position());
        }
        //add a ModuleNode that represents the aspect
        ModuleNode aspectNode = ext.moduleStruct.addAspectNode(
                member.name(), (CPEName)member.getCPE(), member.position());
        assert(aspectNode != null);
        aspectNode = ext.moduleStruct.addMember(module.name(), aspectNode);
        //should always add properly, since we already checked if there is an
        // existing aspect
        assert(aspectNode != null);
    }

    public void checkSigMember(ModuleDecl module, SigMember sigMember,
            ExtensionInfo ext) {
        //TODO: Check signature member (typecheck?)
        //add signature member
        ModuleNodeModule n = (ModuleNodeModule)ext.moduleStruct.getNode(module.name(),
                ModuleNode.TYPE_MODULE);
        assert(n != null);
        n.addSigMember(sigMember);
    }

    public ModuleBody_c reconstruct(List members, List sigMembers) {
        if (!CollectionUtil.equals(members, this.members)
                || !CollectionUtil.equals(sigMembers, this.sigMembers)) {
            ModuleBody_c n = (ModuleBody_c) copy();
            n.members = members;
            n.sigMembers = sigMembers;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {
        List newMembers = new LinkedList();
        List newSigMembers = new LinkedList();

        //visit members
        for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModMember member = (ModMember) iter.next();
            newMembers.add(visitChild(member, v));
        }

        //visit signature
        for (Iterator iter = sigMembers.iterator(); iter.hasNext();) {
            SigMember sigMember = (SigMember) iter.next();
            newSigMembers.add(visitChild(sigMember, v));
        }

        return reconstruct(newMembers, newSigMembers);
    }

}

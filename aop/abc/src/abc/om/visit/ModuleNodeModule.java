/* abc - The AspectBench Compiler
 * Copyright (C) 2005
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.SootClass;

import abc.aspectj.ast.CPEName_c;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PatternMatcher;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.om.ast.SigMember;
import abc.om.ast.SigMemberAdvertiseDecl;
import abc.om.weaving.aspectinfo.BoolPointcut;
import abc.om.weaving.aspectinfo.OMClassnamePattern;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.NotPointcut;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Within;

/**
 * @author Neil Ongkingco
 *
 */
public class ModuleNodeModule extends ModuleNode {
    private List /* ModuleNode */members = null;

    private List /* SigMember */sigMembers = null;

    //pointcut which is the disjuction of the sigMembers;
    private abc.weaving.aspectinfo.Pointcut sigAIPointcut = null;
    private abc.weaving.aspectinfo.Pointcut privateSigAIPointcut = null;
    
    //true if the module is included as a constrained module
    //only valid for modules
    private boolean isConstrained = false;

    //pointcut which is a conjunction of !within(A) where A is a member of the
    // module. Initially, this pointcut contains only the immediate members
    // (class
    // and aspect), and does not include the extPointcuts of its children.
    // Once a call to getExtPointcut occurs, the whole extPointcut is built
    // by traversing the subtree rooted at the module.
    private abc.weaving.aspectinfo.Pointcut extPointcut = null;

    private boolean extPointcutBuilt = false;
    
    private Aspect dummyAspect = null;
    
    private boolean isRoot = false;
    
    public ModuleNodeModule(String name, boolean isRoot) {
        this.name = name;
        members = new LinkedList();
        //initializer values for AIPointcuts
        sigAIPointcut = new BoolPointcut(false, AbcExtension.generated);
        privateSigAIPointcut = new BoolPointcut(false, AbcExtension.generated);
        this.isRoot = isRoot;
    }
    
    public boolean isRoot() {
        return this.isRoot;
    }
    
    public void setIsConstrained(boolean isConstrained) {
        this.isConstrained = isConstrained;
    }
    
    public boolean isConstrained() {
        return isConstrained;
    }
    
    public void addMember(ModuleNode node) {
        members.add(node);

        //add
        if (node.isAspect() || node.isClass()) {
            Pointcut pc = makeExtPointcut(node);
            if (extPointcut == null) {
                extPointcut = pc;
            } else {
                extPointcut = AndPointcut.construct(extPointcut, pc,
                        AbcExtension.generated);
            }
        }
    }

    private Pointcut makeExtPointcut(ModuleNode node) {
        assert(node.isAspect() || node.isClass());

        //create !within(node.name) pointcut
        ClassnamePatternExpr cpe = null;
        if (node.isClass()) {
            cpe = ((ModuleNodeClass)node).getCPE();
        } else if (node.isAspect()) {
            cpe = new CPEName_c(AbcExtension.generated, 
                    		((ModuleNodeAspect)node).getAspectNamePattern());
        }
        assert(cpe != null);

        ClassnamePattern namePattern = new OMClassnamePattern(cpe);
        Pointcut pc = new Within(namePattern, AbcExtension.generated);
        pc = NotPointcut.construct(pc, AbcExtension.generated);
        return pc;
    }

    //returns the extPointcut. If it is not yet completely built, it builds
    //the entire subtree rooted at the module;
    //This should not be called until the module tree has been built
    public Pointcut getExtPointcut() {
        assert(isModule()); //only allowable for modules
        
        if (!extPointcutBuilt) {
            extPointcutBuilt = true;
            for (Iterator iter = members.iterator(); iter.hasNext();) {
                ModuleNode member = (ModuleNode) iter.next();
                if (!member.isModule()) {
                    continue;
                }
                Pointcut member_pc = ((ModuleNodeModule)member).getExtPointcut();
                if (member_pc != null) {
                    extPointcut = AndPointcut.construct(extPointcut, 
                            member_pc, 
                            AbcExtension.generated);
                }
            }
        }
        return extPointcut;
    }

    public void addSigMember(SigMember sigMember) {
        if (sigMembers == null) {
            sigMembers = new LinkedList();
        }
        sigMembers.add(sigMember);
        
        //update the AIPointcuts
        abc.weaving.aspectinfo.Pointcut newPointcut = sigMember.getAIPointcut();
        //if a method sig member, add the ext pointcut
        if (sigMember instanceof SigMemberAdvertiseDecl) {
            //get the topmost ancestor of this modulenode, then use its extPointcut
            ModuleNodeModule root = this;
            while (root.getParent() != null) {
                root = (ModuleNodeModule)root.getParent();
            }
            abc.weaving.aspectinfo.Pointcut extPointcut = root.getExtPointcut();
            //ext pointcut can be null (i.e. if the module has no members
            if (extPointcut != null) {
                newPointcut = AndPointcut.construct(
                        newPointcut, 
                        root.getExtPointcut(), 
                        AbcExtension.generated);
            }
        }
        if (sigMember.isPrivate()) {
            privateSigAIPointcut = OrPointcut.construct(privateSigAIPointcut, 
                    newPointcut, AbcExtension.generated);
        } else {
            sigAIPointcut = OrPointcut.construct(sigAIPointcut, 
                    newPointcut, AbcExtension.generated);
        }
    }

    public Pointcut getSigAIPointcut() {
        return sigAIPointcut;
    }
    
    public Pointcut getPrivateSigAIPointcut() {
        return privateSigAIPointcut;
    }

    public List getMembers() {
        return members;
    }

    public List getSigMembers() {
        return sigMembers;
    }
    
    //only for finding modules and aspects
    public boolean containsMember(String name, int type) {
        if (members == null)
            return false;
        for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModuleNode member = (ModuleNode) iter.next();
            if (name.compareTo(member.name()) == 0 && member.type() == type) {
                return true;
            }
        }
        return false;
    }

    //only for finding modules and aspects
    public boolean containsMember(ModuleNode n) {
        return containsMember(n.name(), n.type());
    }

    //for classes, also for aspect PCNodes
    public boolean containsMember(PCNode node) {
        if (members == null) {
            return false;
        }
        for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModuleNode member = (ModuleNode) iter.next();
            //check if the CPE matches the node
            if (member.isClass()) {
                ModuleNodeClass memberClass = (ModuleNodeClass) member;
                if (memberClass.getCPE().matches(node)) {
                    return true;
                }
            } else if (member.isAspect()) {
                ModuleNodeAspect memberAspect = (ModuleNodeAspect) member;
                if (memberAspect.getAspectNode() == node) {
                    return true;
                }
            }
        }
        return false;
    }
    
    //Gets the names of the aspects that belong to this module and its children
    //Should only be called on modules
    public Set /*<String>*/ getAspectNames() {
        assert (this.isModule());
        Set ret = new HashSet();
        for (Iterator iter = members.iterator(); iter.hasNext(); ) {
            ModuleNode member = (ModuleNode) iter.next();
            if (member.isAspect()) {
                ret.add(member.name());
            }
            if (member.isModule()) {
                ModuleNodeModule memberModule = (ModuleNodeModule) member; 
                ret.addAll(memberModule.getAspectNames());
            }
        }
        return ret;
    }
    
    public void normalizeSigPointcut() {
        Pointcut newPc = this.sigAIPointcut;
        if (newPc != null) {
	        //Only need to normalize the sigAIPointcut
	        newPc = Pointcut.normalize(newPc, 
	                			new LinkedList(), 
	                			getDummyAspect());
	        this.sigAIPointcut = newPc;
        }
    }
    
    public Aspect getDummyAspect() {
        return dummyAspect;
    }
    
    public void setDummyAspect(Aspect dummyAspect) {
        this.dummyAspect = dummyAspect;
    }
    
    public boolean isAspect() {
        return false;
    }
    public boolean isClass() {
        return false;
    }
    public boolean isModule() {
        return true;
    }
    public int type() {
        return ModuleNode.TYPE_MODULE;
    }
}

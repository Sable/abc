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
 * Created on May 15, 2005
 *
 */
package abc.om.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import abc.aspectj.ast.CPEName;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.ast.PointcutDecl;
import abc.aspectj.types.AspectType_c;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.om.ast.SigMember;
import abc.om.weaving.matching.OMMatchingContext;
import abc.polyglot.util.ErrorInfoFactory;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Per;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Singleton;
import abc.weaving.matching.ConstructorCallShadowMatch;
import abc.weaving.matching.GetFieldShadowMatch;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.MethodCallShadowMatch;
import abc.weaving.matching.SetFieldShadowMatch;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.OrResidue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.Weaver;

/**
 * Internal representation of the entire module specification.
 * 
 * @author Neil Ongkingco
 *  
 */
public class ModuleStructure {

    private Map /* <String, ModuleNodeModule> */moduleNodes;

    private Map /* <String, ModuleNodeAspect> */aspectNodes;

    private Map /* <String, ModuleNodeClass> */classNodes;

    private ExtensionInfo ext;

    //pseudo-singleton, just so that OMMethodCall can access ModuleStructure
    // without knowing ext.
    private static ModuleStructure instance;

    //caches
    // Caching seems to make openmod run a bit slower (at least for ants)
    // I'm leaving the code in (but commented) until I get a larger test case
    // to see it's worth implementing
    //private Map /* <PCNode,ModuleNode> */ownerCache;
    //private Map /* <ModuleNode,List> */moduleListCache;
    //private Map /* <PCNode,Pointcut> */sigCache;

    public ModuleStructure(ExtensionInfo ext) {
        moduleNodes = new HashMap();
        aspectNodes = new HashMap();
        classNodes = new HashMap();
        //caching
        /*
         * ownerCache = new HashMap(); moduleListCache = new HashMap(); sigCache =
         * new HashMap();
         */
        ModuleStructure.instance = this;
        this.ext = ext;
    }

    private Map getMap(int type) {
        switch (type) {
        case ModuleNode.TYPE_ASPECT:
            return aspectNodes;
        case ModuleNode.TYPE_CLASS:
            return classNodes;
        case ModuleNode.TYPE_MODULE:
            return moduleNodes;
        }
        return null;
    }

    public static ModuleStructure v() {
        return ModuleStructure.instance;
    }

    //only for modules
    public ModuleNode addModuleNode(String name, boolean isRoot, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n != null) {
            return null;
        }
        n = new ModuleNodeModule(name, isRoot, pos);
        nodeMap.put(n.name(), n);
        return n;
    }

    //for aspect members
    public ModuleNode addAspectNode(String name, CPEName cpe, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_ASPECT);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n != null) {
            return null;
        }
        n = new ModuleNodeAspect(name, cpe, pos);
        nodeMap.put(n.name(), n);
        return n;
    }

    //for class members
    public ModuleNode addClassNode(String parentName, ClassnamePatternExpr cpe, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_CLASS);
        ModuleNode n = new ModuleNodeClass(parentName, cpe, pos);
        nodeMap.put(n.name(), n);
        return n;
    }

    /**
     * Adds a member to a module node. Returns the node of the member on
     * success, null on error (didn't want to make a new exception)
     */
    public ModuleNode addMember(String name, ModuleNode member) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n == null) {
            return null;
        }

        if (member.getParent() != null) {
            return null;
        }
        member.setParent(n);
        ((ModuleNodeModule) n).addMember(member);
        return member;
    }

    /**
     * Adds a signature member to a module node. Returns the module on success,
     * null on error
     */
    public ModuleNode addSigMember(String name, SigMember sigMember) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n == null) {
            return null;
        }

        ((ModuleNodeModule) n).addSigMember(sigMember);
        return n;
    }

    /**
     * Returns the node that matches the given name and type
     */
    public ModuleNode getNode(String name, int type) {
        Map nodeMap = getMap(type);
        return (ModuleNode) nodeMap.get(name);
    }

    /**
     * Returns the owner of an aspect.
     */
public ModuleNode getOwner(String name, int type) {
    	//TODO: Just get the parent of the ModuleNodeAspect
        assert(type == ModuleNode.TYPE_ASPECT) : "Node is not an aspect node";
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ModuleNode n = (ModuleNode) iter.next();
            if (n instanceof ModuleNodeModule) {
	            if (((ModuleNodeModule)n).containsMember(name, type)) {
	                return n;
	            }
            }
        }
        return null;
    }
    /**
     * Gets the owner of the class/aspect represented by node
     */
    public ModuleNode getOwner(PCNode node) {
        ModuleNode ret = null;

        //caching
        /*
         * ret = (ModuleNode)ownerCache.get(node); if (ret != null) { return
         * ret; }
         */

        //iterate through all module nodes
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ret = (ModuleNode) iter.next();
            if (ret.isModule() && ((ModuleNodeModule) ret).containsMember(node)) {
                //caching
                //ownerCache.put(node,ret);
                return ret;
            }
        }
        return null;
    }

    public boolean hasMultipleOwners(PCNode node) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        boolean foundOnce = false;
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ModuleNode n = (ModuleNode) iter.next();
            if (n.isModule() && ((ModuleNodeModule) n).containsMember(node)) {
                if (foundOnce == false) {
                    foundOnce = true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the modules other than the given module and its children. If node is
     * null, return all modules
     */
    public Collection getOtherModules(ModuleNode node) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        if (node != null && !node.isModule()) {
            throw new InternalCompilerError(
                    "Expecting a ModuleNode of type TYPE_MODULE");
        }
        List otherModules = new LinkedList();
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ModuleNode currNode = (ModuleNode) iter.next();
            //if currNode is a module, and does not have node as an ancestor,
            // include it
            if (currNode.isModule()) {
                ModuleNode parentNode = currNode;
                boolean found = false;
                while (parentNode.getParent() != null) {
                    if (parentNode == node) {
                        found = true;
                        break;
                    }
                    parentNode = parentNode.getParent();
                }
                if (!found) {
                    otherModules.add(currNode);
                }
            }
        }
        return otherModules;
    }

    //Returns true if the aspect and the class are in the same module path
    //i.e. if the aspect is in a module that is an ancestor of the class.
    //Also true if both the aspect and the class are not in modules.
    //Note that aspectNode can be null, meaning that the aspect is not in a
    //module
    public boolean isInSameModuleSet(ModuleNode aspectNode, PCNode classNode) {
        if (aspectNode != null && !aspectNode.isAspect()) {
            throw new InternalCompilerError(
                    "Expecting a ModuleNode of type TYPE_ASPECT");
        }
        ModuleNode classOwner = getOwner(classNode);

        //if the aspect is not in a module, and so is the class, then return
        // true
        if (aspectNode == null && classOwner == null) {
            return true;
        }
        //if the aspect is not in a module but the class is, return false
        if (aspectNode == null && classOwner != null) {
            return false;
        }

        ModuleNode aspectOwner = aspectNode.getParent();
        //if both unconstrained by modules, return true
        if (classOwner == null && aspectOwner == null) {
            return true;
        }
        //if the class is not in a module, but the aspect is, return true
        //TODO: This decision means that aspects in modules _can_ access
        // classes that are not in modules
        if (classOwner == null && aspectOwner != null) {
            return true;
        }
        //if the class is in a module an the aspect is not, return false\
        //this should already be handled by another case above
        if (classOwner != null && aspectOwner == null) {
            assert(false) : "ERROR: Unable to determine isInSameModuleSet. Possible ModuleStructure corruption.";
            return false;
        }
        //if both are in a module, see if the aspect belongs to a module that
        //is the same as the owner of the class or an ancestor thereof
        //also checks if the inclusion was not constrained
        ModuleNode prev = null;
        while (classOwner != null) {
            if (classOwner == aspectOwner && 
                    (prev == null || !((ModuleNodeModule)prev).isConstrained())) {
                return true;
            }
            prev = classOwner;
            classOwner = classOwner.getParent();
        }

        return false;
    }
    /**
     * Returns the module list of the given node. For a module, the module list
     * is the module itself and its ancestors, starting from the module itself.
     * For aspects and classes, the module list is the module list of its
     * parent.
     */
    public List getModuleList(ModuleNode n) {
        List ret;

        //caching
        /*
         * ret = (List)moduleListCache.get(n); if (ret != null) { return ret; }
         */

        //iterate to get list of ancestors
        ret = new ArrayList();
        if (n.isModule()) {
            ret.add(n);
        }
        while (n.getParent() != null) {
            n = n.getParent();
            ret.add(n);
        }
        return ret;
    }

    /**
     * Returns the pointcut that represents the signatures that apply to the
     * class TODO: Get rid of the iteration, by normalizing the modules...
     */
    public Pointcut getApplicableSignature(PCNode classNode) {
        Pointcut ret = null;

        //caching
        /*
         * ret = (Pointcut)sigCache.get(classNode); if (ret != null) { return
         * ret; }
         */

        ModuleNodeModule owner = (ModuleNodeModule) getOwner(classNode);
        if (owner == null) {
            return ret;
        }

        //get the private signature for the owning module
        ret = owner.getPrivateSigAIPointcut();

        boolean prevIsConstrained = false;
        //get the non-private signatures from the modules in the modulelist
        List /* ModuleNode */moduleList = getModuleList(owner);
        for (Iterator iter = moduleList.iterator(); iter.hasNext();) {
            ModuleNodeModule module = (ModuleNodeModule) iter.next();
            if (prevIsConstrained) {
                //  (currPC && (childPC)) || (childPC &&
                // thisAspect(currModule.aspects))
                ret = OrPointcut
                        .construct(AndPointcut.construct(ret, module
                                .getSigAIPointcut(), AbcExtension.generated),
                                AndPointcut.construct(ret, module
                                        .getThisAspectPointcut(),
                                        AbcExtension.generated),
                                AbcExtension.generated);
            } else {
                ret = OrPointcut.construct(ret, module.getSigAIPointcut(),
                        AbcExtension.generated);
            }
            prevIsConstrained = module.isConstrained();
        }

        //caching
        //sigCache.put(classNode,ret);
        return ret;
    }

    /**
     * Checks result of the match taking the effect of signatures into account
     * 
     * @author Neil Ongkingco
     *  
     */
    public Residue openModMatchesAt(Pointcut pc, ShadowMatch sm,
            Aspect currAspect, WeavingEnv weaveEnv, SootClass cls,
            SootMethod method, AbstractAdviceDecl ad) throws SemanticException {

        Residue ret = pc.matchesAt(new MatchingContext(weaveEnv, cls, method,
                sm));

        //if openmod is not loaded, just return ret
        if (!AbcExtension.isLoaded()) {
            return ret;
        }
        //if it doesn't match, return immediately
        if (ret == NeverMatch.v()) {
            return ret;
        }
        //get the class the method belongs to
        //note: Used to be a method getOwningClass() of ShadowMatch+,
        //but moved here to avoid contamination of the base code. And yes, it
        // is ugly.
        SootClass sootOwningClass = null;
        if (sm instanceof MethodCallShadowMatch) {
            sootOwningClass = ((MethodCallShadowMatch) sm).getMethodRef()
                    .declaringClass();
        } else if (sm instanceof ConstructorCallShadowMatch) {
            sootOwningClass = ((ConstructorCallShadowMatch) sm).getMethodRef()
                    .declaringClass();
        } else if (sm instanceof GetFieldShadowMatch) {
            sootOwningClass = ((GetFieldShadowMatch) sm).getFieldRef()
                    .declaringClass();
        } else if (sm instanceof SetFieldShadowMatch) {
            sootOwningClass = ((SetFieldShadowMatch) sm).getFieldRef()
                    .declaringClass();
        } else {
            sootOwningClass = sm.getContainer().getDeclaringClass();
        }

        PCNode owningClass = PCStructure.v().getClass(sootOwningClass);

        //get the class that contains this statement
        SootClass sootContainingClass = sm.getContainer().getDeclaringClass();
        PCNode containingClass = PCStructure.v().getClass(sootContainingClass);

        //debug
        AbcExtension.debPrintln("ModuleStructure.matchesAt: aspect "
                + currAspect.getName() + "; owning class "
                + owningClass.toString() + "; containing class "
                + containingClass.toString() + "; pc " + pc.toString());

        //if the aspect and the class belong to the same moduleset, return ret
        //i.e. it is matching in with an internal class/aspect, so signatures
        // are
        //not applied
        ModuleStructure ms = ModuleStructure.v();
        ModuleNode aspectNode = ms.getNode(currAspect.getName(),
                ModuleNode.TYPE_ASPECT);
        if (ms.isInSameModuleSet(aspectNode, owningClass)) {
            return ret;
        }
        //check if any of the signatures match this shadow
        Pointcut sigPointcut = ms.getApplicableSignature(owningClass);
        Residue sigMatch;

        //if there are no matching signatures, return nevermatch (that is,
        //the owning module did not expose any point in the class)
        if (sigPointcut == null) {
            return NeverMatch.v();
        }

        //match the signature with the current shadow
        try {
            sigMatch = sigPointcut.matchesAt(new OMMatchingContext(weaveEnv, sm
                    .getContainer().getDeclaringClass(), sm.getContainer(), sm,
                    currAspect));
        } catch (SemanticException e) {
            throw new InternalCompilerError("Error matching signature pc", e);
        }

        //if the signature matches, conjoin the residue with the existing
        // residue
        if (sigMatch != NeverMatch.v()) {
            Residue retResidue;
            //special case for cflowsetup, as cflow pointcuts should not
            //apply to the cflowsetups, otherwise the counter
            // increment/decrement
            //would never be called
            if (ad instanceof CflowSetup) {
                retResidue = ret;
            } else {
                retResidue = AndResidue.construct(sigMatch, ret);
            }
            //debug
            AbcExtension.debPrintln("sigMatch = " + sigMatch);
            AbcExtension.debPrintln("ret = " + ret);
            AbcExtension.debPrintln("retResidue = " + retResidue);

            return retResidue;
        } else {
            //else throw a no signature match warning
            AbcExtension.debPrintln("No matching signature in class "
                    + containingClass + " of advice in aspect "
                    + currAspect.getName());

            ModuleNode ownerModule = ms.getOwner(owningClass);
            String msg = "An advice in aspect " + currAspect.getName()
                    + " would normally apply here, "
                    + "but does not match any of the signatures of module "
                    + ownerModule.name();

            addWarning(msg, sm);

            return NeverMatch.v();
        }
    }

    public ModuleNode getTopAncestor(ModuleNode member) {
        ModuleNode ret = member.getParent();
        while (ret.getParent() != null) {
            ret = ret.getParent();
        }
        return ret;
    }
    
    private static void addWarning(String msg, ShadowMatch sm) {
        abc.main.Main.v().error_queue.enqueue(ErrorInfoFactory.newErrorInfo(
                ErrorInfo.WARNING, msg, sm.getContainer(), sm.getHost()));
    }

    public Collection /* <ModuleNodes> */getModules() {
        return moduleNodes.values();
    }

    public void normalizeSigPointcuts() {
        for (Iterator iter = moduleNodes.values().iterator(); iter.hasNext();) {
            ModuleNode currNode = (ModuleNode) iter.next();
            if (currNode.isModule()) {
                ((ModuleNodeModule) currNode).normalizeSigPointcut();
            }
        }
    }
}

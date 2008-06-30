/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Neil Ongkingco
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

package abc.ja.om.modulestruct;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.SootClass;
import soot.SootMethod;
import abc.aspectj.ast.CPEName;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.visit.PCNode;
import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.Pattern;
import abc.ja.om.jrag.TypeDecl;
import abc.ja.om.AbcExtension;
import abc.om.modulestruct.ModuleNode;
import abc.om.modulestruct.ModuleNodeAspect;
import abc.om.modulestruct.ModuleNodeModule;
import abc.om.modulestruct.ModuleStructure;
import abc.om.visit.ModulePrecedence;
import abc.om.weaving.matching.OMMatchingContext;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.aspectinfo.DeclareSoft;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.ConstructorCallShadowMatch;
import abc.weaving.matching.GetFieldShadowMatch;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.MethodCallShadowMatch;
import abc.weaving.matching.SetFieldShadowMatch;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

public class JAModuleStructure extends ModuleStructure {
	
	//TODO: Find a way to remove this. This was added just for omComputeModulePrecedence
	public Collection<String> aspect_names;
	
	public JAModuleStructure() {
		super();
		//TODO: Remove this
		aspect_names = new HashSet();
	}
	
    public ModuleNode addModuleNode(String name, boolean isRoot, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n != null) {
            return null;
        }
        n = new JAModuleNodeModule(name, isRoot, pos);
        nodeMap.put(n.name(), n);
        return n;
    }

	public ModuleNode addAspectNode(String name, CPEName cpe, Position pos) {
		throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.addAspectNode");
    }
	public ModuleNode addAspectNode(String name, Pattern cpe, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_ASPECT);
        ModuleNode n = (ModuleNode) nodeMap.get(name);
        if (n != null) {
            return null;
        }
        n = new JAModuleNodeAspect(name, cpe, pos);
        nodeMap.put(n.name(), n);
        return n;
    }
	
    public ModuleNode addClassNode(String parentName, ClassnamePatternExpr cpe, Position pos) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.addAspectNode");
    }
    public ModuleNode addClassNode(String parentName, Pattern cpe, Position pos) {
        Map nodeMap = getMap(ModuleNode.TYPE_CLASS);
        ModuleNode n = new JAModuleNodeClass(parentName, cpe, pos);
        nodeMap.put(n.name(), n);
        return n;
    }
    public ModuleNode getOwner(String name, int type) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.getOwner(String, int)");
    }
    public ModuleNode getOwner(PCNode node) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.getOwner(PCNode)");
    }
    public boolean hasMultipleOwners(PCNode node) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.hasMultipleOwners(PCNode)");
    }
    public boolean isInSameModuleSet(ModuleNode aspectNode, PCNode classNode) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.isInSameModuleSet(ModuleNode, PCNode)");
    }
    public Pointcut getApplicableSignature(PCNode classNode) {
    	throw new InternalCompilerError("Attempt to use polyglot version JAModuleStructure.getApplicableSignature(PCNode)");
    }
    
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
        ((JAModuleNodeModule) n).addMember(member);
        return member;
    }
    
    
    
    public boolean hasMultipleOwners(TypeDecl type) {
    	Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
    	boolean foundOnce = false;
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ModuleNode n = (ModuleNode) iter.next();
            if (n.isModule() && ((JAModuleNodeModule) n).containsClassMember(type)) {
                if (foundOnce == false) {
                    foundOnce = true;
                } else {
                    return true;
                }
            }
        }
    	return false;
    }
    
    public boolean hasMultipleFriendOwners(AspectDecl aspect) {
    	Map nodeMap = getMap(ModuleNode.TYPE_MODULE);
    	boolean foundOnce = false;
        for (Iterator iter = nodeMap.values().iterator(); iter.hasNext();) {
            ModuleNode n = (ModuleNode) iter.next();
            if (n.isModule() && ((JAModuleNodeModule) n).containsFriendMember(aspect)) {
                if (foundOnce == false) {
                    foundOnce = true;
                } else {
                    return true;
                }
            }
        }
    	return false;
    }

    public ModuleNode getOwner(SootClass sc) {
    	ModuleNode ret = null;
    	for (Iterator i = getMap(ModuleNode.TYPE_CLASS).values().iterator();
    		i.hasNext(); ) {
    		JAModuleNodeClass classNode = (JAModuleNodeClass) i.next();
    		if (classNode.getCPEPattern().matchesType(sc)) {
    			return classNode.getParent();
    		}
    	}
    	return ret;
    }
    
    public ModuleNode getOwner(TypeDecl t) {
    	ModuleNode ret = null;
    	for (Iterator i = getMap(ModuleNode.TYPE_CLASS).values().iterator();
    		i.hasNext(); ) {
    		JAModuleNodeClass classNode = (JAModuleNodeClass) i.next();
    		if (classNode.getCPEPattern().matchesType(t)) {
    			return classNode.getParent();
    		}
    	}
    	return ret;
    }
    
    
    
    //reimplementation
    public boolean isInSameModuleSet(ModuleNode aspectNode, ModuleNode classOwner) {
        if (aspectNode != null && !aspectNode.isAspect()) {
            throw new InternalCompilerError(
                    "Expecting a ModuleNode of type TYPE_ASPECT");
        }

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
    
    public boolean isInSameModuleSet(ModuleNode aspectNode, SootClass sc) {
    	return isInSameModuleSet(aspectNode, getOwner(sc));
    }
    
    public Residue openModMatchesAt(Pointcut pc, ShadowMatch sm,
            Aspect currAspect, WeavingEnv weaveEnv, SootClass cls,
            SootMethod method, AbstractAdviceDecl ad) throws SemanticException {

        Residue ret = pc.matchesAt(new MatchingContext(weaveEnv, cls, method,
                sm));

        //if it doesn't match, return immediately
        if (ret == NeverMatch.v()) {
            return ret;
        }
        //if it is a declare advice decl, then return original match
        //NOTE: This means that declare warning, error messages ignore module
        //pointcuts. Think about this later.
        if (ad instanceof DeclareMessage || 
        	ad instanceof DeclareSoft) {
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

        //get the class that contains this statement
        SootClass sootContainingClass = sm.getContainer().getDeclaringClass();

        //debug
        AbcExtension.debPrintln(
                AbcExtension.OMDebug.MATCHING_DEBUG,
                "\n-------------------------\nModuleStructure.matchesAt: aspect "
                + currAspect.getName() + "; shadowmatch " + sm.toString() + "; owning class "
                + sootOwningClass.getName() + "; pc " + pc.toString());

        //if the aspect and the class belong to the same moduleset, return ret
        //i.e. it is matching in with an internal class/aspect, so signatures
        // are
        //not applied
        JAModuleStructure ms = ((abc.ja.om.AbcExtension) abc.main.Main.v().getAbcExtension()).moduleStruct;
        ModuleNode aspectNode = ms.getNode(currAspect.getName(),
                ModuleNode.TYPE_ASPECT);
        if (ms.isInSameModuleSet(aspectNode, sootOwningClass)) {
            return ret;
        }
        //check if any of the signatures match this shadow
        Pointcut sigPointcut = ms.getApplicableSignature(sootOwningClass);
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
            AbcExtension.debPrintln(AbcExtension.OMDebug.MATCHING_DEBUG,
                    "sigMatch = " + sigMatch);
            AbcExtension.debPrintln(AbcExtension.OMDebug.MATCHING_DEBUG,
                    "ret = " + ret);
            AbcExtension.debPrintln(AbcExtension.OMDebug.MATCHING_DEBUG,
                    "retResidue = " + retResidue);

            return retResidue;
        } else {
            //else throw a no signature match warning
            AbcExtension.debPrintln(AbcExtension.OMDebug.MATCHING_DEBUG,
                    "No matching signature in class "
                    + " of advice in aspect "
                    + currAspect.getName());

            ModuleNode ownerModule = ms.getOwner(sootOwningClass);
            String msg = "An advice in aspect " + currAspect.getName()
                    + " would normally apply here, "
                    + "but does not match any of the signatures of module "
                    + ownerModule.name();

            addWarning(msg, sm);

            return NeverMatch.v();
        }
    }
    
    public Pointcut getApplicableSignature(SootClass sc) {
        Pointcut ret = null;


        ModuleNodeModule owner = (ModuleNodeModule) getOwner(sc);
        if (owner == null) {
            return ret;
        }

        //get the private signature for the owning module
        ret = owner.getPrivateSigAIPointcut();

        boolean prevIsConstrained = false;
        //get the non-private signatures from the modules in the modulelist
        List /* ModuleNode */moduleList = getModuleAncestorList(owner);
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

        return ret;
    }

}

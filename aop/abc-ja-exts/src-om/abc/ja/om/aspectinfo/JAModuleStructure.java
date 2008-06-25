package abc.ja.om.aspectinfo;

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
import abc.om.visit.ModuleNode;
import abc.om.visit.ModuleNodeAspect;
import abc.om.visit.ModuleNodeModule;
import abc.om.visit.ModulePrecedence;
import abc.om.visit.ModuleStructure;
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
		//TODO: Move this
	    mod_prec_rel = new HashMap(); /*<ModulePrecedence, Set <ModulePrecedence>>*/
	    extAspectMap = new HashMap(); /*<String ,ExtAspect>*/
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
    
    
    
    //reimplementation
    public boolean isInSameModuleSet(ModuleNode aspectNode, SootClass sc) {
        if (aspectNode != null && !aspectNode.isAspect()) {
            throw new InternalCompilerError(
                    "Expecting a ModuleNode of type TYPE_ASPECT");
        }
        ModuleNode classOwner = getOwner(sc);

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


    private void printPrecRel() {
        //debug: print out precedence relation
		AbcExtension abcExt = (abc.ja.om.AbcExtension) abc.main.Main.v().getAbcExtension();
    	Map<String,Set<String>> prec_rel = abcExt.getGlobalAspectInfo().getPrecedenceRelation();
        for (Iterator iter = prec_rel.keySet().iterator();
        		iter.hasNext();) {
            String aspectName = (String) iter.next();
            AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG, 
                    aspectName + " : ");
            Set laterAspects = (Set)prec_rel.get(aspectName);
            
            for (Iterator iter2 = laterAspects.iterator(); iter2.hasNext(); ) {
                String laterAspectName = (String) iter2.next();
                AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG, 
                        laterAspectName + "; ");
            }
            
            AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG, "");
        }
    }
    
    private HashMap mod_prec_rel; /*<ModulePrecedence, Set <ModulePrecedence>>*/
    private HashMap extAspectMap; /*<String ,ExtAspect>*/
    
    //Representation of an external aspect in mod_prec_rel
    private class ExtAspect implements ModulePrecedence{
        String name;
        public ExtAspect(String name) {
            this.name = name;
        }
        public String name() {
            return name;
        }
        public String toString() {
            return name;
        }
        public Set getAspectNames() {
            Set ret = new HashSet();
            ret.add(name);
            return ret;
        }
    }
    private ExtAspect getExtAspect(String name) {
        if (extAspectMap.get(name) == null) {
            extAspectMap.put(name, new ExtAspect(name));
        }
        return (ExtAspect)extAspectMap.get(name);
    }

    private Set getLaterSet(Map map, ModulePrecedence key) {
        if (map.get(key) == null) {
            map.put(key,new HashSet());
        }
        return (Set)map.get(key);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see abc.aspectj.visit.OncePass#once() 
     * OM: Order all the modules and update the precedence relation accordingly
     * This enforces the coherence of precedence within a module. For example,
     * given the module specification
     * 
     * module M {
     * 		friend A,B,C;
     * } 
     * 
     * no external aspect is allowed to insert itself between the aspects A, B 
     * and C. This ensures the author of a module that his aspects will execute 
     * in the _exact_ order that he has specified them, without any intervening
     * aspects. This makes it easier to make assumptions about the any common 
     * store being used by the aspects in a module.
     * 
     * This ordering also applies to included modules.
     * 
     * Unrelated modules can be ordered by using a declare precedence statement
     * that relates friend aspects of the modules. For example:
     * 
     * module M1{ friend A,B;}
     * module M2{ friend C,D;}
     * aspect A { declare precedence : A,C;}
     * 
     * The declare precedence statement orders the unrelated modules M1 and M2 so
     * that all the aspects of M1 come before M2. This is a rather 'hacked' 
     * implementation as "declare precedence : M1,M2;" would have been more 
     * intuitive, but this would have required the module namespace to be 
     * accessible from aspects.
     * 
     * The implementation orders the top level modules and external aspects
     * in a total order using a topological sort. Top level modules are used
     * as modules rooted at a top-level module are already explicitly ordered
     * by the order they are included.
     */
    //TODO: Check this fully
    public void omComputeModulePrecedence() {
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                "---OMComputeModulePrecedence");
		abc.ja.om.AbcExtension abcExt = (abc.ja.om.AbcExtension) abc.main.Main.v().getAbcExtension();
		Map<String,Set<String>> prec_rel = abcExt.getGlobalAspectInfo().getPrecedenceRelation();
		
        Collection modules = abcExt.moduleStruct.getModules();
        
        //first, put in the module order implied by declare precedence
        // statements and module inclusion foreach top level module
        topmodule:
        for (Iterator iter = modules.iterator(); iter.hasNext();) {
            ModuleNodeModule currModule = (ModuleNodeModule) iter.next();
            if (currModule.getParent() != null) {continue;}
            //Create an entry in the mod_prec_rel 
            getLaterSet(mod_prec_rel, currModule);
            
        //	foreach aspect in the top level module
            Collection currAspects = currModule.getAspectNames();
            for (Iterator aspectIter = currAspects.iterator(); 
            		aspectIter.hasNext();) {
                String currAspect = (String) aspectIter.next();
                Set laterAspects = (Set)prec_rel.get(currAspect);
                
        //		foreach later aspect
                lateraspect:
                for (Iterator laspectIter = laterAspects.iterator(); 
                		laspectIter.hasNext();) {
                    String currLaterAspectName = (String) laspectIter.next();
                    ModuleNodeAspect currLaterAspect = 
                        (ModuleNodeAspect)
                        	abcExt.moduleStruct.getNode(currLaterAspectName, 
                                		ModuleNode.TYPE_ASPECT);
                    
        //			if external aspect add extaspect to module relation, and continue
                    if (currLaterAspect == null) {
                        ExtAspect extAspect = getExtAspect(currLaterAspectName);
                        //check for a cycle
                        if (hasHigherPrecedence(extAspect,currModule)) {
                            addExtAspectCycleError(extAspect, currModule);
                            continue topmodule;
                        }
                        getLaterSet(mod_prec_rel,currModule).add(extAspect);
                        continue lateraspect;
                    }
                    
        //			get laterAspectRoot = top level module of the later aspect
                    ModuleNodeModule laterAspectRoot = 
                        (ModuleNodeModule) 
                        	abcExt.moduleStruct.getTopAncestor(currLaterAspect);
                    
        //			if laterAspectRoot == top level module, continue to next
                    if (laterAspectRoot == currModule) {continue lateraspect;}
                    
        //			check for cycles
                    if (hasHigherPrecedence(laterAspectRoot, currModule)) {
                        AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                                "The modules " + currModule + 
                                " and " + laterAspectRoot + "have a precedence conflict.");
                        addModuleCycleError(currModule, laterAspectRoot);
                        continue topmodule;
                    }
                    
        //			set laterAspectRoot to be of later precedence than the top level
        // 			module
                    Set laterModules = getLaterSet(mod_prec_rel, currModule); 
                    laterModules.add(laterAspectRoot);
                }//end lateraspect
            }//end memberaspects
        }//end topmodule1
        
        //Get external aspects and add them to the module relation
        extaspects:
        for (Iterator iter = aspect_names.iterator(); iter.hasNext();) {
            String extAspectName = (String) iter.next();
            //if not an external aspect, continue
            if (abcExt.moduleStruct.getNode(extAspectName, ModuleNode.TYPE_ASPECT) != null) {
                continue extaspects;
            }
            //Add the entries implied by the aspect prec_rel into the 
            //module precedence relation 
            Set laterAspectNames = (Set)prec_rel.get(extAspectName);
            if (laterAspectNames == null || laterAspectNames.size() == 0) {
                //just add the extAspect to the list and proceed to the next
                getLaterSet(mod_prec_rel,getExtAspect(extAspectName));
                continue extaspects;
            }
            extaspectlater:
            for (Iterator iter2 = laterAspectNames.iterator(); iter2.hasNext();) {
                String currLAspect = (String) iter2.next();
                //if internal aspect, add topancestor to extaspect's laterset
                ModuleNode node = abcExt.moduleStruct.getNode(currLAspect, ModuleNode.TYPE_ASPECT); 
                if ( node != null) {
                    Set extAspLSet = getLaterSet(mod_prec_rel, getExtAspect(extAspectName));
                    extAspLSet.add(abcExt.moduleStruct.getTopAncestor(node));
                    continue extaspectlater;
                }
                //if external aspect, just add to the later set
                Set extAspLSet = getLaterSet(mod_prec_rel, getExtAspect(extAspectName));
                extAspLSet.add(getExtAspect(currLAspect));
            }
        }

        //DEBUG
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,"---mod_prec_rel");
        debPrintPrecRel(mod_prec_rel);

        //topologically sort the top level modules, and enforce precedence
        //		(creating any warnings for modules that did not have explicitly
        // 		declared precedence)
        LinkedList sorted = topologicalSort(mod_prec_rel);
        if (sorted == null) {
            return;
        }
        //DEBUG
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,"---Sorted");
        AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG,"[");
        for (Iterator i = sorted.iterator(); i.hasNext(); ) {
            AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                    i.next().toString() + "; ");
        }
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,"]");
        
        //TODO: Add warnings when top level module precedence wasn't explicitly
        //defined
        
        //generate the prec_rel defined by the topological sort
        Set/*<String>*/ prevAspectNames = new HashSet();
        while (sorted.size() > 0) {
            ModulePrecedence mp = (ModulePrecedence)sorted.removeLast();
            Set/*<String>*/ aspectNames = mp.getAspectNames();
            for (Iterator i = aspectNames.iterator(); i.hasNext();) {
                String aspectName = (String) i.next();
                if (prec_rel.get(aspectName) == null) {
                    prec_rel.put(aspectName, new HashSet());
                }
                Set laterAspects = (Set) prec_rel.get(aspectName);
                laterAspects.addAll(prevAspectNames);
            }
            prevAspectNames.addAll(aspectNames);
        }
        
        //DEBUG
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                "Module precedence relation");
        debPrintPrecRel(mod_prec_rel);
        AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                "Aspect precedence relation");
        debPrintPrecRel(prec_rel);
    }
    
    //true if m1 has higher precedence than m2
    private boolean hasHigherPrecedence(ModulePrecedence m1, ModulePrecedence m2) {
    	
        Set m1Set = (Set)mod_prec_rel.get(m1);
        if (m1Set == null) {
            return false;
        }
        return m1Set.contains(m2);
    }
    
    //Error message queueing
    private void addModuleCycleError(ModuleNodeModule m1, ModuleNodeModule m2) {
        ErrorInfo err = new ErrorInfo(ErrorInfo.SEMANTIC_ERROR,
                "The module " + m2.name() + 
                " or one of its included modules is in precedence conflict with the module " +
                m1.name() + " or one of its included modules.",
                m2.position()
        );
        abc.main.Main.v().getAbcExtension().reportError(err);
    }
    //Error message queueing
    private void addExtAspectCycleError(ExtAspect ext, ModuleNodeModule m) {
        ErrorInfo err = new ErrorInfo(ErrorInfo.SEMANTIC_ERROR,
                "The module " + m.name() + 
                " or one of its included modules are in precedence conflict with the external aspect " +
                ext.name(),
                m.position()
        );
        abc.main.Main.v().getAbcExtension().reportError(err);
    }
    //Error message queueing
    private void addTopSortCycleError(Set /*<String>*/ aspectNames) {
        String msg = "The following aspects are involved in a precedence cycle(s): ";
        for (Iterator i = aspectNames.iterator(); i.hasNext(); ) {
            msg += i.next().toString();
            if (i.hasNext()) {
                msg += ", ";
            }
        }
        ErrorInfo err = new ErrorInfo(ErrorInfo.SEMANTIC_ERROR,
                msg,
                AbcExtension.generated
        );
        abc.main.Main.v().getAbcExtension().reportError(err);
    }

    //Topological sort on the precedence relation
    private LinkedList/*<ModulePrecedence>*/ topologicalSort(Map map) {
        LinkedList ret = new LinkedList();
        //Create (semi)deep copy of map  
        HashMap map_copy = new HashMap();
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            map_copy.put(key, ((HashSet)map.get(key)).clone());
        }
        
        boolean changed = false;
        Set removed = new HashSet(); 
        do {
            removed.clear();
            changed = false;
            for (Iterator i = map_copy.keySet().iterator(); i.hasNext();) {
                Object curr = i.next();
                Set currSet =(Set)map_copy.get(curr); 
                if (currSet.size() == 0) {
                    map_copy = removePrecEntry(map_copy, curr);
                    ret.addFirst(curr);
                    removed.add(curr);
                    changed = true;
                }
            }
            for (Iterator i = removed.iterator(); i.hasNext(); ) {
                map_copy.remove(i.next());
            }
        } while (changed);
        if (map_copy.size() > 0) {
            Set cycledAspects = new HashSet();
            for (Iterator i = map_copy.values().iterator(); i.hasNext(); ) {
                Set currSet = (Set) i.next();
                for (Iterator j = currSet.iterator(); j.hasNext(); ) {
                    ModulePrecedence mp = (ModulePrecedence)j.next();
                    cycledAspects.addAll(mp.getAspectNames());
                }
            }
            addTopSortCycleError(cycledAspects);
            return null;
        }
        return ret;
    }
    
    //Utility function used by topological sort
    //Just remove the entry from the values. It is removed from the keyset after
    //the iteration
    private HashMap removePrecEntry(HashMap map, Object entry) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            HashSet value = (HashSet)map.get(key);
            value.remove(entry);
        }
        return map;
    }

    private void debPrintPrecRel(Map map) {
        Set keys = map.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            Object currEntry = iter.next();
            AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                    currEntry.toString() + " : [");
            
            Set laterEntries = (Set)map.get(currEntry);
            for (Iterator iter2 = laterEntries.iterator(); iter2.hasNext();) {
                Object laterEntry = iter2.next();
                AbcExtension.debPrint(AbcExtension.OMDebug.PRECEDENCE_DEBUG,
                        laterEntry.toString() + "; ");
            }
            AbcExtension.debPrintln(AbcExtension.OMDebug.PRECEDENCE_DEBUG,"]");
        }
    }

}

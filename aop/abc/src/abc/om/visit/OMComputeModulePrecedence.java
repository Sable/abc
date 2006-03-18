/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Neil Ongkingco
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

import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.util.ErrorInfo;
import abc.aspectj.visit.OncePass;
import abc.aspectj.visit.PCStructure;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.polyglot.util.ErrorInfoFactory;

import java.security.InvalidParameterException;
import java.util.*;


/**
 * @author Neil Ongkingco
 * Order the modules and external aspects into a single precedence list.
 * This ensures that an external aspect cannot come between two internal 
 * aspects.
 */
public class OMComputeModulePrecedence extends OncePass {

    private ExtensionInfo ext;

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

    public OMComputeModulePrecedence(Pass.ID id, Job job, ExtensionInfo ext) {
        super(id);
        this.ext = ext;
        mod_prec_rel = new HashMap();
        extAspectMap = new HashMap();
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
    protected void once() {
        AbcExtension.debPrintln("---OMComputeModulePrecedence");
        
        Collection modules = ext.moduleStruct.getModules();
        
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
            memberaspects:
            for (Iterator aspectIter = currAspects.iterator(); 
            		aspectIter.hasNext();) {
                String currAspect = (String) aspectIter.next();
                Set laterAspects = (Set)ext.prec_rel.get(currAspect);
                
        //		foreach later aspect
                lateraspect:
                for (Iterator laspectIter = laterAspects.iterator(); 
                		laspectIter.hasNext();) {
                    String currLaterAspectName = (String) laspectIter.next();
                    ModuleNodeAspect currLaterAspect = 
                        (ModuleNodeAspect)
                        	ext.moduleStruct.getNode(currLaterAspectName, 
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
                        	ext.moduleStruct.getTopAncestor(currLaterAspect);
                    
        //			if laterAspectRoot == top level module, continue to next
                    if (laterAspectRoot == currModule) {continue lateraspect;}
                    
        //			check for cycles
                    if (hasHigherPrecedence(laterAspectRoot, currModule)) {
                        AbcExtension.debPrint("The modules " + currModule + 
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
        for (Iterator iter = ext.aspect_names.iterator(); iter.hasNext();) {
            String extAspectName = (String) iter.next();
            //if not an external aspect, continue
            if (ext.moduleStruct.getNode(extAspectName, ModuleNode.TYPE_ASPECT) != null) {
                continue extaspects;
            }
            //Add the entries implied by the aspect prec_rel into the 
            //module precedence relation 
            Set laterAspectNames = (Set)ext.prec_rel.get(extAspectName);
            if (laterAspectNames == null || laterAspectNames.size() == 0) {
                //just add the extAspect to the list and proceed to the next
                getLaterSet(mod_prec_rel,getExtAspect(extAspectName));
                continue extaspects;
            }
            extaspectlater:
            for (Iterator iter2 = laterAspectNames.iterator(); iter2.hasNext();) {
                String currLAspect = (String) iter2.next();
                //if internal aspect, add topancestor to extaspect's laterset
                ModuleNode node = ext.moduleStruct.getNode(currLAspect, ModuleNode.TYPE_ASPECT); 
                if ( node != null) {
                    Set extAspLSet = getLaterSet(mod_prec_rel, getExtAspect(extAspectName));
                    extAspLSet.add(ext.moduleStruct.getTopAncestor(node));
                    continue extaspectlater;
                }
                //if external aspect, just add to the later set
                Set extAspLSet = getLaterSet(mod_prec_rel, getExtAspect(extAspectName));
                extAspLSet.add(getExtAspect(currLAspect));
            }
        }

        //DEBUG
        debPrintPrecRel(mod_prec_rel);

        //topologically sort the top level modules, and enforce precedence
        //		(creating any warnings for modules that did not have explicitly
        // 		declared precedence)
        LinkedList sorted = topologicalSort(mod_prec_rel);
        if (sorted == null) {
            return;
        }
        //DEBUG
        AbcExtension.debPrint("[");
        for (Iterator i = sorted.iterator(); i.hasNext(); ) {
            AbcExtension.debPrint(i.next().toString() + "; ");
        }
        AbcExtension.debPrintln("]");
        
        //TODO: Add warnings when top level module precedence wasn't explicitly
        //defined
        
        //generate the prec_rel defined by the topological sort
        Set/*<String>*/ prevAspectNames = new HashSet();
        while (sorted.size() > 0) {
            ModulePrecedence mp = (ModulePrecedence)sorted.removeLast();
            Set/*<String>*/ aspectNames = mp.getAspectNames();
            for (Iterator i = aspectNames.iterator(); i.hasNext();) {
                String aspectName = (String) i.next();
                if (ext.prec_rel.get(aspectName) == null) {
                    ext.prec_rel.put(aspectName, new HashSet());
                }
                Set laterAspects = (Set) ext.prec_rel.get(aspectName);
                laterAspects.addAll(prevAspectNames);
            }
            prevAspectNames.addAll(aspectNames);
        }
        
        //DEBUG
        AbcExtension.debPrintln("Module precedence relation");
        debPrintPrecRel(mod_prec_rel);
        AbcExtension.debPrintln("Aspect precedence relation");
        debPrintPrecRel(ext.prec_rel);
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
        abc.main.Main.v().error_queue.enqueue(err);
    }
    //Error message queueing
    private void addExtAspectCycleError(ExtAspect ext, ModuleNodeModule m) {
        ErrorInfo err = new ErrorInfo(ErrorInfo.SEMANTIC_ERROR,
                "The module " + m.name() + 
                " or one of its included modules are in precedence conflict with the external aspect " +
                ext.name(),
                m.position()
        );
        abc.main.Main.v().error_queue.enqueue(err);
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
        abc.main.Main.v().error_queue.enqueue(err);
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
            AbcExtension.debPrint(currEntry.toString() + " : [");
            
            Set laterEntries = (Set)map.get(currEntry);
            for (Iterator iter2 = laterEntries.iterator(); iter2.hasNext();) {
                Object laterEntry = iter2.next();
                AbcExtension.debPrint(laterEntry.toString() + "; ");
            }
            AbcExtension.debPrintln("]");
        }
    }
}

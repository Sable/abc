/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMPerSymbolAdviceDecl;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.tagkit.InstructionShadowTag;

/**
 * Adds tags to each unit which is matched by at least one tracematch symbol but only
 * if it is the first such unit for a given shadow. (To avoid stuttering.)
 * @author Eric Bodden
 */
public class MatchingTMSymbolTagger {

    
    /**
     * Temporary set only used internally.
     */
    protected transient Set processedShadows = new HashSet();

    /**
     * Adds tags to each unit which is matched by at least one tracematch symbol but only
     * if it is the first such unit for a given shadow. (To avoid stuttering.)
     */
    public void performTagging() {
        Set weavableClasses = Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses();
        //for all weavable classes
        for (Iterator iter = weavableClasses.iterator(); iter.hasNext();) {
            AbcClass abcClass = (AbcClass) iter.next();
            SootClass sc = abcClass.getSootClass();
            
            //for all methods
            for (Iterator mIter = sc.getMethods().iterator(); mIter.hasNext();) {
                SootMethod m = (SootMethod) mIter.next();
                
                //for all units
                for (Iterator uIter = m.getActiveBody().getUnits().iterator(); uIter.hasNext();) {
                    Unit u = (Unit) uIter.next();
                    u = Main.v().getAbcExtension().getWeaver().rebind(u);
                    
                    //add the tag
                    addMatchingSymbolsTag(u, m);
                }
            }
        }
    }


    /**
     * Adds the tag.
     * @param u a unit
     * @param m the surrounding method
     */
    protected void addMatchingSymbolsTag(Unit u, SootMethod m) {
        List matchingSymbolIDs = getMatchingSymbolIDsIfFirst(u, m);
        if(!matchingSymbolIDs.isEmpty()) {
            u.addTag(new MatchingTMSymbolTag(matchingSymbolIDs));
        }
    }
    
    /**
     * Returns the list of matching symbols but only if <code>u</code> is the
     * first matched unit at that shadow.
     * @param u a unit
     * @param m the surrounding method
     * @return IDs of symbols matching the given unit, but only if
     * <code>u</code> is the first matched unit for the given shadow;
     * the empty set otherwise
     */
    protected List getMatchingSymbolIDsIfFirst(Unit u, SootMethod m) {
        if(u.hasTag(InstructionShadowTag.NAME)) {
            
            InstructionShadowTag tag = (InstructionShadowTag) u.getTag(InstructionShadowTag.NAME);
            Integer shadowId = new Integer(tag.value());
            if(!processedShadows.contains(shadowId)) {                
                processedShadows.add(shadowId);
                return getMatchingSymbolIDs(u, m);
            }
                
        }
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Returns the list of matching symbols for a unit.
     * @param u a unit
     * @param m the surrounding method
     * @return IDs of symbols matching the given unit
     */
    protected List getMatchingSymbolIDs(Unit u, SootMethod m) {
        if(u.hasTag(InstructionShadowTag.NAME)) {
            //if u has a shadow tag
            
        	//get the tag
        	InstructionShadowTag tag = (InstructionShadowTag) u.getTag(InstructionShadowTag.NAME);
            
        	//get the advice lists for the surrounding method
            MethodAdviceList adviceList = Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(m);
            List adviceApplications = adviceList.allAdvice();        
            List res = new ArrayList(); 
            
            //add the symbols IDs for all TMPerSymbolAdviceDecl whose shadow id
            //matches the one of the tag
            for (Iterator iter = adviceApplications.iterator(); iter.hasNext();) {
                AdviceApplication aa = (AdviceApplication) iter.next();
                
                if((aa.shadowmatch.shadowId == tag.value())
                    && (aa.advice instanceof TMPerSymbolAdviceDecl)) {
                    TMPerSymbolAdviceDecl tmAdvice = (TMPerSymbolAdviceDecl) aa.advice;
                    res.add(tmAdvice.getUniqueSymbolId());                                 
                }
            }
            
            return res;
            
        } else {
            
            return Collections.EMPTY_LIST;
            
        }
    }

}

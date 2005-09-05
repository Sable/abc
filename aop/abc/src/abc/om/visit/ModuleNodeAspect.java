/*
 * Created on Jul 29, 2005
 *
 */
package abc.om.visit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.aspectj.ast.NamePattern;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;

/**
 * @author Neil Ongkingco
 *
 */
public class ModuleNodeAspect extends ModuleNode {
    private PCNode aspectNode; //PCNode for TYPE_ASPECT nodes
    private NamePattern aspectNamePattern; //Name pattern for the aspect
    
    public ModuleNodeAspect(String name, NamePattern namePattern) {
        this.name = name;
        this.aspectNamePattern = namePattern;
        
        //get the PCNode representing the aspect by using PCStructure.matchName
        //TODO: Assumes that aspects are always top-level. Needs to be changed
        // if it is no longer the case
        PCStructure pcStruct = PCStructure.v();
        Set matches = pcStruct.matchName(namePattern, new PCNode(null, null,
                pcStruct), new HashSet(), new HashSet());
        assert(matches.size() == 1);
        Iterator iter = matches.iterator();
        aspectNode = (PCNode) iter.next();
    }
    
    public NamePattern getAspectNamePattern() {
        return aspectNamePattern;
    }
    
    public PCNode getAspectNode() {
        return aspectNode;
    }
    
    public boolean isAspect() {
        return true;
    }
    public boolean isClass() {
        return false;
    }
    public boolean isModule() {
        return false;
    }
    public int type() {
        return ModuleNode.TYPE_ASPECT;
    }
}

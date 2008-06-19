package abc.ja.om.aspectinfo;

import java.util.Iterator;
import java.util.Map;

import polyglot.types.SemanticException;
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
import abc.om.visit.ModuleNode;
import abc.om.visit.ModuleNodeModule;
import abc.om.visit.ModuleStructure;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;

public class JAModuleStructure extends ModuleStructure {
	public JAModuleStructure() {
		super();
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
    public Residue openModMatchesAt(Pointcut pc, ShadowMatch sm,
            Aspect currAspect, WeavingEnv weaveEnv, SootClass cls,
            SootMethod method, AbstractAdviceDecl ad) throws SemanticException {
    	return super.openModMatchesAt(pc, sm, currAspect, weaveEnv, cls, method, ad);
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

}

package abc.ja.om.modulestruct;

import java.util.Iterator;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.visit.PCNode;
import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.OMPointcutMember;
import abc.ja.om.jrag.Pattern;
import abc.ja.om.jrag.TypeDecl;
import abc.om.AbcExtension;
import abc.om.ast.SigMember;
import abc.om.modulestruct.ModuleNode;
import abc.om.modulestruct.ModuleNodeAspect;
import abc.om.modulestruct.ModuleNodeClass;
import abc.om.modulestruct.ModuleNodeModule;
import abc.om.weaving.aspectinfo.BoolPointcut;
import abc.om.weaving.aspectinfo.OMClassnamePattern;
import abc.om.weaving.aspectinfo.ThisAspectPointcut;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.NotPointcut;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Within;

public class JAModuleNodeModule extends ModuleNodeModule {
	public JAModuleNodeModule(String name, boolean isRoot, Position pos) {
		super(name, isRoot, pos);
	}
	
	protected Pointcut makeExtPointcut(ModuleNode node) {
        assert (node.isClass()) : "Parameter is not a class node";

        //create !within(node.name) pointcut
        Pattern cpe = null;
        if (node.isClass()) {
            cpe = ((JAModuleNodeClass)node).getCPEPattern();
        } 
        assert (cpe != null) : "Class node CPE not properly initialized";

        ClassnamePattern namePattern = cpe.classnamePattern();
        	
        Pointcut pc = new Within(namePattern, AbcExtension.generated);
        pc = NotPointcut.construct(pc, AbcExtension.generated);
        return pc;
    }

	@Override
	public void addSigMember(SigMember sigMember) {
		throw new InternalCompilerError("Attempt to add Polyglot SigMember to JAModuleNodeModule");
	}
	
	public void addSigMember(OMPointcutMember pointcutMember) {
		Pointcut newPointcut = pointcutMember.pointcut();
		if (pointcutMember.isAdvertise()) {
			newPointcut = AndPointcut.construct(
                    newPointcut, 
                    this.getExtPointcut(), 
                    abc.ja.om.AbcExtension.generated);
		}
		if (pointcutMember.isPrivate()) {
			privateSigAIPointcut = OrPointcut.construct(privateSigAIPointcut, 
                    newPointcut, AbcExtension.generated);
		} else {
			sigAIPointcut = OrPointcut.construct(sigAIPointcut, 
                    newPointcut, AbcExtension.generated);
		}
	}

	public boolean containsMember(PCNode node) {
		throw new InternalCompilerError("Attempt to use JAModuleNodeModule.containsMember(PCNode) unsupported in abc-ja");
	}
	
    public Pointcut getThisAspectPointcut() {
        Pointcut ret = BoolPointcut.construct(false, AbcExtension.generated);
        for (Iterator iter = members.iterator(); iter.hasNext(); ) {
            ModuleNode currMember = (ModuleNode) iter.next();
            
            //if not an aspect member, proceed to next
            if (!(currMember instanceof ModuleNodeAspect)) {continue;}
            
            JAModuleNodeAspect aspectMember = (JAModuleNodeAspect) currMember;
            Pointcut newTerm = ThisAspectPointcut.construct(aspectMember.getCPEPattern().classnamePattern(),
            		AbcExtension.generated); 
            ret = OrPointcut.construct(ret, newTerm, AbcExtension.generated);
        }
        return ret;
    }
    
    public boolean containsClassMember(TypeDecl t) {
    	if (members == null) {
    		return false;
    	}
    	for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModuleNode member = (ModuleNode) iter.next();
            //check if the CPE matches the node
            if (member.isClass()) {
            	if (((JAModuleNodeClass)member).getCPEPattern().matchesType(t)) {
            		return true;
            	}
            }
        }
    	return false;
    }
    
    public boolean containsFriendMember(AspectDecl t) {
    	if (members == null) {
    		return false;
    	}
    	for (Iterator iter = members.iterator(); iter.hasNext();) {
            ModuleNode member = (ModuleNode) iter.next();
            //check if the CPE matches the node
            if (member.isAspect()) {
            	if (((JAModuleNodeAspect)member).getCPEPattern().matchesType(t)) {
            		return true;
            	}
            }
        }
    	return false;
    }
    

}

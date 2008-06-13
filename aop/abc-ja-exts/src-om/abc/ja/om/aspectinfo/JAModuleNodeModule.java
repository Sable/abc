package abc.ja.om.aspectinfo;

import java.util.Iterator;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.visit.PCNode;
import abc.ja.om.jrag.Pattern;
import abc.om.AbcExtension;
import abc.om.ast.SigMember;
import abc.om.visit.ModuleNode;
import abc.om.visit.ModuleNodeAspect;
import abc.om.visit.ModuleNodeClass;
import abc.om.visit.ModuleNodeModule;
import abc.om.weaving.aspectinfo.BoolPointcut;
import abc.om.weaving.aspectinfo.OMClassnamePattern;
import abc.om.weaving.aspectinfo.ThisAspectPointcut;
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

}

/*
 * Created on Jun 18, 2005
 *
 */
package abc.om.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AspectBody_c;
import abc.aspectj.ast.AspectDecl_c;
import abc.aspectj.ast.IsSingleton_c;
import abc.aspectj.ast.PerClause_c;
import abc.om.AbcExtension;

/**
 * @author Neil Ongkingco
 * Dummy aspect declaration used by all modules. Contains a list of all the modules.
 */
public class DummyAspectDecl_c extends AspectDecl_c {
    List /*ModuleDecl*/ modules;
    
    public DummyAspectDecl_c(Position pos, String moduleName) {
        super(pos, 
                false, 
                Flags.NONE, 
                moduleName + "_DummyAspect", 
                null, 
                new ArrayList(), 
                new IsSingleton_c(pos), 
                new AspectBody_c(pos, new ArrayList())); 
        modules = new ArrayList();
    }
    
    public void addModule(ModuleDecl module) {
        modules.add(module);
    }
    
    public List getModules() {
        return modules;
    }
    
    public DummyAspectDecl_c reconstruct(List modules, Node superRet) {
        if (!CollectionUtil.equals(this.modules, modules)) {
            DummyAspectDecl_c n = (DummyAspectDecl_c) superRet.copy();
            n.modules = modules;
            return n;
        }
        return (DummyAspectDecl_c)superRet;
    }
    
    public Node visitChildren(NodeVisitor v) {
        AbcExtension.debPrintln("DummyAspectDecl_c.visitChildren " + v.toString());
        Node ret = super.visitChildren(v);
        List newModules = new LinkedList();
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            ModuleDecl moduleDecl = (ModuleDecl) iter.next();
            newModules.add(visitChild(moduleDecl, v));
        }
        
        return reconstruct(newModules, ret);
    }
}

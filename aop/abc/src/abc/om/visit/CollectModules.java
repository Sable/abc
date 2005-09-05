/*
 * Created on May 14, 2005
 *
 */
package abc.om.visit;

import abc.om.ExtensionInfo;
import abc.om.ast.ModuleBody;
import abc.om.ast.ModuleDecl;
import abc.om.ast.OpenModNodeFactory;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Adds all module names to ExtensionInfo.modules. Throws an error if a duplicate
 * module name is found.
 * 
 * @author Neil Ongkingco
 *
 */
public class CollectModules extends ContextVisitor {
    private ExtensionInfo ext;
    
    public CollectModules(Job job, TypeSystem ts, OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        //adds module to ModuleStruct, throws an error if there is a duplicate.
        if (n instanceof ModuleDecl) {
            ModuleDecl decl = (ModuleDecl) n;
            ModuleNode node = 
                ext.moduleStruct.addModuleNode(decl.name());
            if (node == null) {
                throw new SemanticException("Duplicate module name", decl.namePos());
            }
            
        }
        return super.enterCall(parent, n);
    }
    
}

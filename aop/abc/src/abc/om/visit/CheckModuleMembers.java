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
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * @author Neil Ongkingco
 *
 */
public class CheckModuleMembers extends ContextVisitor {
    private ExtensionInfo ext;
    
    public CheckModuleMembers(Job job, TypeSystem ts, OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        if (n instanceof ModuleBody) {
//          check if the module members exist in the hierarchy.
            ModuleBody mBody = (ModuleBody) n;
            mBody.checkMembers((ModuleDecl) parent, ext);
        }
        return super.enterCall(parent, n);
    }
}

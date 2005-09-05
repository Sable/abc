/*
 * Created on May 30, 2005
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
 *  Checks the signature members of the module. Comes after checkmembers, as
 */
public class CheckModuleSigMembers extends ContextVisitor {
    
    private ExtensionInfo ext;
    
    public CheckModuleSigMembers(Job job, TypeSystem ts, OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        if (n instanceof ModuleBody) {
            //  check the signature members of the module
            ModuleBody mBody = (ModuleBody) n;
            mBody.checkSigMembers((ModuleDecl) parent, ext);
        }
        return super.enterCall(parent, n);
    }
    
}
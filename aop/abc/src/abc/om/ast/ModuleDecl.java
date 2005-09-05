/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import polyglot.ast.TopLevelDecl;
import polyglot.util.Position;

/**
 * @author Neil Ongkingco
 *
 */
public interface ModuleDecl extends TopLevelDecl {
    public Position namePos();
}

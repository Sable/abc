/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import polyglot.ast.ClassBody;
import polyglot.ast.Node;
import polyglot.types.SemanticException;

import java.util.*;

import abc.om.ExtensionInfo;

/**
 * @author Neil Ongkingco
 *  
 */
public interface ModuleBody extends Node {
    public List members();

    public List sigMembers();

    public void checkMembers(ModuleDecl module, ExtensionInfo ext)
            throws SemanticException;
    public void checkSigMembers(ModuleDecl module, ExtensionInfo ext);

}
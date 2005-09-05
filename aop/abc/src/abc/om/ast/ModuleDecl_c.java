/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.LinkedList;

import abc.aspectj.types.AJFlags;

import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.types.Flags;
import polyglot.util.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * @author Neil Ongkingco
 *  
 */
public class ModuleDecl_c extends Node_c implements ModuleDecl {
    private ModuleBody body;

    private String name;

    private Position namePos;

    public ModuleDecl_c(Position pos, String name, ModuleBody body,
            Position namePos) {
        super(pos);
        this.name = name;
        this.body = body;
        this.namePos = namePos;
    }

    public Flags flags() {
        return new AJFlags();
    }

    public String name() {
        return name;
    }

    public Position namePos() {
        return this.namePos;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("module " + name);
        w.newline();
        body.prettyPrint(w, pp);
    }

    public ModuleDecl_c reconstruct(ModuleBody body) {
        if (body != this.body) {
            ModuleDecl_c n = (ModuleDecl_c) copy();
            n.body = body;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {
        //Nothing changes
        ModuleBody body = (ModuleBody) visitChild(this.body, v);

        return reconstruct(body);
    }
}
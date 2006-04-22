/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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
import polyglot.types.Named;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;

/**
 * Represents the entire module declarations. Contains the module
 * name and the module body.
 * @author Neil Ongkingco
 *  
 */
public class ModuleDecl_c extends Node_c implements ModuleDecl {
    private ModuleBody body;

    private String name;

    private Position namePos;
    
    private boolean isRoot = false;

    public ModuleDecl_c(Position pos, String name, ModuleBody body,
            Position namePos, boolean isRoot) {
        super(pos);
        this.name = name;
        this.body = body;
        this.namePos = namePos;
        this.isRoot = isRoot;
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
    
    public boolean isRoot() {
        return isRoot;
    }
    
    /* Required by Polyglot 1.3.2 */
    public Named declaration() {
    	return new Named() {
    		public String name() {
    			return name;
    		}
    		public String fullName() {
    			return namePos.file();
    		}
    		public TypeSystem typeSystem() {
    			return null;
    		}
    	    public Object copy() {
    	    	return null;
    	    }
    	    public boolean isCanonical() {
    	    	return false;
    	    }
    	    public boolean equalsImpl(TypeObject t) {
    	    	return false;
    	    }
    	    public Position position() {
    	    	return namePos();
    	    }
    	};
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

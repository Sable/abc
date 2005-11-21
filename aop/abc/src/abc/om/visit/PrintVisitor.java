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
 * Created on May 14, 2005
 *
 */
package abc.om.visit;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import abc.aspectj.ast.PointcutDecl;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.eaj.ast.GlobalPointcutDecl;
import abc.eaj.extension.EAJAdviceDecl;
import abc.om.AbcExtension;
import abc.om.ExtensionInfo;
import abc.om.ast.ModuleDecl;
import abc.om.ast.OpenModNodeFactory;
import abc.om.ast.SigMember;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * @author Neil Ongkingco
 *  
 */
public class PrintVisitor extends ContextVisitor {
    private OpenModNodeFactory nf;

    private ExtensionInfo ext;

    public PrintVisitor(Job job, TypeSystem ts, OpenModNodeFactory nf,
            ExtensionInfo ext) {
        super(job, ts, nf);
        this.nf = nf;
        this.ext = ext;
    }

    public NodeVisitor enter(Node parent, Node n) {
        if (AbcExtension.debug == false) {
            return super.enter(parent, n);
        }
        if (n instanceof ModuleDecl) {
            ModuleDecl decl = (ModuleDecl) n;
            CodeWriter w = new CodeWriter(System.out, 80);
            PrettyPrinter p = new PrettyPrinter();
            w.write("From AST: ");
            w.newline();
            p.printAst(n, w);
            
            w.write("From ModuleStructure: ");
            w.newline();
            printFromModuleStructure((ModuleDecl) n, w);
            printOtherModules(((ModuleDecl) n).name(), w);
        }
        //test for getApplicableSignatures
        /*
        if (n instanceof ClassDecl) {
            CodeWriter w = new CodeWriter(System.out, 80);
            ClassDecl decl = (ClassDecl) n;
            PCNode classNode = PCStructure.v().getClass(decl.type());
            w.write("Getting applicable signatures for class "
                    + classNode.toString());
            w.newline();
            w.write("START APPL SIGNATURES");
            w.newline();
            Pointcut sig_pc = ext.moduleStruct.getApplicableSignature(classNode);
            if (sig_pc != null) {
                w.write(sig_pc.toString());
                w.newline();
            }
            w.write("END APPL SIGNATURES");
            w.newline();
            try {
                w.flush();
            } catch (IOException e) {
            }

        }*/
        
        return super.enter(parent, n);
    }

    private void printFromModuleStructure(ModuleDecl decl, CodeWriter w) {
        ModuleNodeModule n = (ModuleNodeModule)ext.moduleStruct.getNode(decl.name(),
                ModuleNode.TYPE_MODULE);
        assert(n != null) : "Node is null";
        //print the module name
        w.write("module " + decl.name());
        w.newline();
        w.write("isConstrained = " + n.isConstrained());
        w.newline();
        w.begin(4);
        w.write("/*members*/");
        w.newline();
        //print the module members
        List members = n.getMembers();
        if (members != null) {
            for (Iterator iter = members.iterator(); iter.hasNext();) {
                ModuleNode member = (ModuleNode) iter.next();
                switch (member.type()) {
                case ModuleNode.TYPE_CLASS:
                    w.write("class ");
                    PrettyPrinter p = new PrettyPrinter();
                    ((ModuleNodeClass)member).getCPE().prettyPrint(w, p);
                    break;
                case ModuleNode.TYPE_ASPECT:
                    w.write("aspect ");
                    w.write(member.name());
                    break;
                case ModuleNode.TYPE_MODULE:
                    w.write("module ");
                    w.write(member.name());
                    break;
                }
                w.newline();
            }
        }
        //print the module sigmembers
        w.write("/*signature*/");
        w.newline();
        Pointcut sig_pc = n.getSigAIPointcut();
//      some modules can contain all private signatures
        if (sig_pc != null) {
            w.write(sig_pc.toString());
        	w.newline();
        }
        w.write("/*ext pointcut*/");
        w.newline();
        Pointcut ext_pc = n.getExtPointcut();
        if (ext_pc != null) {
	        w.write(ext_pc.toString());
	        w.newline();
        }
        w.write("/*thisAspect pointcut*/");
        Pointcut thisAspectPc = n.getThisAspectPointcut();
        w.write(thisAspectPc.toString());
        w.newline();
        try {
            w.flush();
        } catch (IOException e) {
        }
    }

    private void printOtherModules(String name, CodeWriter w) {
        ModuleNode node = ext.moduleStruct
                .getNode(name, ModuleNode.TYPE_MODULE);
        Collection otherModules = ext.moduleStruct.getOtherModules(node);
        w.write("Other modules (" + name + "): ");
        for (Iterator iter = otherModules.iterator(); iter.hasNext();) {
            ModuleNode currNode = (ModuleNode) iter.next();
            w.write(currNode.name() + "; ");
        }
        w.newline();
        try {
            w.flush();
        } catch (IOException e) {
        }
    }

    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        n = super.leave(parent, old, n, v);

        return n;
    }
}

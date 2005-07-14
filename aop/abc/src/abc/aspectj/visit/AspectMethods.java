/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Julian Tibble
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

package abc.aspectj.visit;

import java.util.List;
import java.util.Stack;
import java.util.Iterator;
import java.util.LinkedList;

import polyglot.util.InternalCompilerError;

import polyglot.ast.JL;
import polyglot.ast.Node;
import polyglot.ast.MethodDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ClassDecl;
import polyglot.ast.NodeFactory;
import polyglot.ast.Local;
import polyglot.ast.Field;
import polyglot.ast.Call;
import polyglot.ast.New;
import polyglot.ast.Special;
import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.Receiver;
import polyglot.ast.Binary;

import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;

import polyglot.frontend.Job;
import polyglot.main.Report;

import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;

import abc.aspectj.ast.PCIf;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AspectDecl;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.PointcutDecl;
import abc.aspectj.ast.ProceedCall;

import abc.aspectj.ast.IntertypeDecl;
import abc.aspectj.ast.IntertypeFieldDecl;
import abc.aspectj.ast.IntertypeMethodDecl;
import abc.aspectj.ast.IntertypeFieldDecl_c;
import abc.aspectj.ast.IntertypeMethodDecl_c;
import abc.aspectj.ast.IntertypeConstructorDecl_c;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.AspectDecl_c;
import abc.aspectj.ast.MakesAspectMethods;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import polyglot.visit.ContextVisitor;

/**
 * 
 * @author Oege de Moor
 * @author Julian Tibble
 */
public class AspectMethods extends ContextVisitor {

    private Stack /* List MethodDecl */ methods; // method declaration lists, one for each classdecl
    private Stack /* List Formal */ formals; // pointcut formals for generating if-methods
    private Stack /* MethodDecl */ proceeds; // dummy proceed methods for transforming proceed calls
    private Stack /* AdviceDecl */ advices;
    private Stack /* ParsedClassType */ container; // Keep track of current container
    private Stack /* IntertypeDecl */ itd;     
    private Stack /* Expr */ lhss; /* left-hand sides of assignments */ 
    private Stack /* PCIf */ pcifs;


	public AJNodeFactory nf;
	public AJTypeSystem ts;
	
	public AspectMethods(Job job, NodeFactory nf, TypeSystem ts) {
		super(job, ts, nf);
		this.nf = (AJNodeFactory) nf;
		this.ts = (AJTypeSystem) ts;
		this.methods = new Stack();
		this.formals = new Stack();
		this.proceeds = new Stack();
		this.advices = new Stack();
		this.container = new Stack();
		this.itd = new Stack();
		this.lhss = new Stack();
		this.pcifs = new Stack();
	}

        public void pushClass()
        {
                methods.push(new LinkedList());   
        }

        public void popClass()
        {
                methods.pop();
        }

        public void addMethod(MethodDecl md)
        {
                ((List) methods.peek()).add(md);
        }

        public List /* MethodDecl */ methods()
        {
                return (List) methods.peek();
        }

        public void pushFormals(List /* Formal */ fs)
        {
                formals.push(fs);
        }

        public List /* Formal */ formals()
        {
                return (List) formals.peek();
        }

        public void popFormals()
        {
                formals.pop();
        }

        public void pushProceedFor(AdviceDecl ad)
        {
                proceeds.push(ad.proceedDecl(nf,ts));
        }

        public MethodDecl proceed()
        {
                return (MethodDecl) proceeds.peek();
        }

        public void popProceed()
        {
                proceeds.pop();
        }

        public void pushAdvice(AdviceDecl ad)
        {
                advices.push(ad);
        }

        public AdviceDecl advice()
        {
                return (AdviceDecl) advices.peek();
        }

        public boolean isAdvice()
        {
                return ! advices.isEmpty();
        }

        public void popAdvice()
        {
                advices.pop();
        }

        public void pushContainer(ParsedClassType c)
        {
                container.push(c);
        }

        public ParsedClassType container()
        {
                return (ParsedClassType) container.peek();
        }

        public void popContainer()
        {
                container.pop();
        }

        public void pushIntertypeDecl(IntertypeDecl i)
        {
                itd.push(i);
        }

        public IntertypeDecl intertypeDecl()
        {
                return (IntertypeDecl) itd.peek();
        }

        public void popIntertypeDecl()
        {
                itd.pop();
        }

        public void pushLhs(Expr lhs)
        {
                lhss.push(lhs);
        }

        public Expr lhs()
        {
                return (Expr) lhss.peek();
        }

        public void popLhs()
        {
                lhss.pop();
        }

    public void pushPCIf(PCIf pcif) {
	pcifs.push(pcif);
    }

    public PCIf pcif() {
	return (PCIf) pcifs.peek();
    }

    public boolean isPCIf() {
	return !pcifs.isEmpty();
    }

    public void popPCIf() {
	pcifs.pop();
    }
	
    public NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        // Code to handle aspect methods:
        JL del = n.del();
        if (del instanceof MakesAspectMethods) {
                ((MakesAspectMethods) del).aspectMethodsEnter(this);
        }
		return this;
    }
 
	protected Node leaveCall(Node old, Node n, NodeVisitor v)  throws SemanticException {
		JL del = n.del();    
		if (del instanceof MakesAspectMethods) {
					 n = ((MakesAspectMethods) del).aspectMethodsLeave(this, nf, ts);
				}
		if(del instanceof ClassDecl) {
		    // Emulate ajc's behaviour of turning classes that privileged aspects access public.
		    if(abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getClassesToMakePublic().contains(((ClassDecl)del).type())) {
		        n = ((ClassDecl)n).flags(((ClassDecl)n).flags().Public().clearProtected());
		    }
		}
		return n;
		
	}
    
}

/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.util.Position;

import polyglot.ast.Node;
import polyglot.ast.Expr;
import polyglot.ast.MethodDecl;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Call;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.Call_c;

import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.CodeInstance;

import polyglot.visit.CFGBuilder;
import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.AJContext;

import abc.aspectj.visit.AspectMethods;

/**
 * A reference to "proceed(x1,x2)" inside a piece of around advice.
 * @author Oege de Moor
 *
 */
public class ProceedCall_c extends Call_c
                           implements ProceedCall, MakesAspectMethods
{
	private MethodDecl proceedDecl;
	private CodeInstance ci;
	
	public ProceedCall_c(Position pos, Receiver recv, List arguments) {
		super(pos,recv,"proceed",arguments);		         	
    }
    
    public ProceedCall_c(Call c) {
    	super(c.position(),c.target(),c.name(),c.arguments());
    }
    
    public ProceedCall proceedMethod(MethodDecl md) {
    	return (ProceedCall) name(md.name()).methodInstance(md.methodInstance());
    }
    
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		
		    TypeSystem ts = tc.typeSystem();
		    Context c = tc.context();
		    
		    // register the containing method for later use
		    ci = c.currentCode();
		    
		    // check whether we are in the scope of an advice declaration,
		    // and retrieve proceed's intended type
		    MethodInstance mi = ((AJContext)c).proceedInstance();
			if (mi==null)
			     throw new SemanticException ("proceed can only be used in around advice",position());
			 
			 // collect types of the actual arguments    
			List argTypes = new ArrayList(arguments.size());
			for (Iterator i = arguments.iterator(); i.hasNext(); ) {
				 Expr e = (Expr) i.next();
				 argTypes.add(e.type());
			}

            // match actuals against formals
			if (! mi.callValid(argTypes))
			   throw new SemanticException ("proceed arguments "+argTypes+
                                            " do not match advice formals "+mi.formalTypes(),position());
             
            TypeNode tn = tc.nodeFactory().CanonicalTypeNode(position(),mi.container());
                                                                
             // rewrite the call                                                   
			return this.methodInstance(mi).target(tn).type(mi.returnType());
	}
	
        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing       
                // visitor.advice().addProceedCall(this.)
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                ProceedCall pc = (ProceedCall) this.copy();
                pc = (ProceedCall) pc.methodInstance(pc.methodInstance().throwTypes(new ArrayList()));
 
                return pc.proceedMethod(visitor.proceed());
        }
}

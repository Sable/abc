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

/*
 * Created on Jul 13, 2004
 *
 */
package abc.aspectj.visit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.frontend.Pass.ID;
import polyglot.types.MethodInstance;
import polyglot.types.TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.ParsedClassType;

import polyglot.frontend.AbstractPass;
import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;

import abc.aspectj.ast.IntertypeMethodDecl_c;
import abc.aspectj.extension.AJClassBody_c;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.InterTypeMethodInstance_c;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

/**
 * @author Oege de Moor
 *
 */
public class JarCheck extends OncePass {

	static String injar = "while weaving into jarred class: ";
	
	AJTypeSystem ts;
	Job job;

	public JarCheck(ID id, Job job, TypeSystem ts) {
		super(id);
		this.ts = (AJTypeSystem) ts;
		this.job = job;
	}

	public void once() {
		ErrorQueue eq = job.compiler().errorQueue();
		for (Iterator wovenJarClasses = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWovenClasses().iterator();
		     wovenJarClasses.hasNext(); ) {
		     	job.compiler().errorQueue();
		     	ClassType jarclass = ((AbcClass) wovenJarClasses.next()).getPolyglotType();
		     	try { ts.checkClassConformance(jarclass); }
		     	catch (SemanticException e) {
		     		eq.enqueue(ErrorInfo.SEMANTIC_ERROR,injar + jarclass + ". " + e.getMessage(),e.position());
		     	}
				try { AJClassBody_c.checkDuplicates(jarclass); }
					catch (SemanticException e) {
					eq.enqueue(ErrorInfo.SEMANTIC_ERROR,injar + jarclass + ". " + e.getMessage(),e.position());
				}
				for (Iterator metIt = jarclass.methods().iterator(); metIt.hasNext(); ) {
					MethodInstance mi = (MethodInstance) metIt.next();
				try { IntertypeMethodDecl_c.overrideMethodCheck(mi); }
				   catch (SemanticException e) {
				   	eq.enqueue(ErrorInfo.SEMANTIC_ERROR,injar + jarclass + ". " + e.getMessage(),e.position());
				   }
				}
							
		     }
	}
}

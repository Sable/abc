/*
 * Created on Jul 13, 2004
 *
 */
package abc.aspectj.visit;

import java.util.Iterator;
import polyglot.frontend.Pass.ID;
import polyglot.types.TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.ParsedClassType;

import polyglot.frontend.AbstractPass;
import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;

import abc.aspectj.extension.AJClassBody_c;
import abc.aspectj.types.AJTypeSystem;

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
		for (Iterator wovenJarClasses = GlobalAspectInfo.v().getWovenClasses().iterator();
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
		     }
	}
}

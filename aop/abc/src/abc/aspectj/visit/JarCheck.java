/*
 * Created on Jul 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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

import abc.aspectj.ast.AJClassBody_c;
import abc.aspectj.types.AspectJTypeSystem;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

/**
 * @author oege
 *
 */
public class JarCheck extends OncePass {

	static String injar = "while weaving into jarred class: ";
	
	AspectJTypeSystem ts;
	Job job;

	public JarCheck(ID id, Job job, TypeSystem ts) {
		super(id);
		this.ts = (AspectJTypeSystem) ts;
		this.job = job;
	}

	public void once() {
		ErrorQueue eq = job.compiler().errorQueue();
		for (Iterator wovenJarClasses = GlobalAspectInfo.v().getWovenClasses().iterator();
		     wovenJarClasses.hasNext(); ) {
		     	job.compiler().errorQueue();
		     	ClassType jarclass = ((AbcClass) wovenJarClasses.next()).getPolyglotType();
		     	try { ts.checkClassConformance(jarclass); 
		     	      AJClassBody_c.checkDuplicates(jarclass); }
		     	catch (SemanticException e) {
		     		eq.enqueue(ErrorInfo.SEMANTIC_ERROR,injar + jarclass + ". " + e.getMessage(),e.position());
		     	}
		     	
		     }
	}
}

package abc.aspectj.visit;

import java.io.File;
import java.util.Iterator;

import polyglot.frontend.*;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

public class CheckPackageNames extends OncePass {
    Job job;

    public CheckPackageNames(Pass.ID id, Job job) {
	super(id);
	this.job=job;
    }

    public void once() {
	for (Iterator weavableClasses = GlobalAspectInfo.v().getWeavableClasses().iterator();
	     weavableClasses.hasNext(); ) {
	    ClassType ctype = ((AbcClass) weavableClasses.next()).getPolyglotType();
	    
	    if(!(ctype instanceof ParsedClassType)) continue;

	    ParsedClassType pctype = (ParsedClassType) ctype;

	    if(!pctype.isTopLevel()) continue;
	    if(!pctype.flags().isPublic()) continue;

	    String classname=pctype.fullName();
	    if(pctype.fromSource()==null) continue; // probably came from a jar
	    String filename=pctype.fromSource().path();
	    
	    int dotindex=filename.lastIndexOf(".");

	    String gotname=filename.substring(0,dotindex).toLowerCase();
	    String expectedname=classname.replace('.',File.separatorChar).toLowerCase();
	    
	    if(!gotname.endsWith(expectedname)) {
		job.compiler().errorQueue().enqueue
		    (ErrorInfo.SEMANTIC_ERROR,"public class "+classname+" cannot be defined in file "+filename);
	    }
	}
    }
}

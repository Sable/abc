/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.da;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abc.da.ast.DANodeFactory;
import abc.da.ast.DANodeFactory_c;
import abc.da.parse.Grm;
import abc.da.types.DATypeSystem;
import abc.da.types.DATypeSystem_c;
import abc.da.visit.AdviceNames;
import abc.da.visit.DAAspectInfoHarvester;
import abc.da.visit.OrphanDependentAdviceFinder;
import abc.da.visit.PushParamNames;
import abc.da.visit.TypeCheckAdviceParams;

/**
 * Extension information for Dependent Advice extension.
 * @author Eric Bodden
 */
public class ExtensionInfo extends abc.eaj.ExtensionInfo
{
    public static final Pass.ID ADVICE_NAMES =
        new Pass.ID("advice-names");
    public static final Pass.ID PUSH_PARAM_NAMES =
        new Pass.ID("push-param-names");
    public static final Pass.ID TYPECHECK_PARAM_NAMES =
        new Pass.ID("typecheck-param-names");
    public static final Pass.ID FIND_ORPHAN_DEPENDENT_ADVICE =
        new Pass.ID("orphan-dependent-advice");
    public static final Pass.ID HARVEST_DA_ASPECT_INFO =
        new Pass.ID("harvest-aspect-info-for-dependent-advice");
    
    
    static {
        // force Topics to load
        new Topics();
    }

    public ExtensionInfo(Collection jar_classes, Collection source_files)
    {
        super(jar_classes, source_files);
    }

    public String compilerName() {
        return "da";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new DANodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new DATypeSystem_c();
    }

    @Override
    public List passes(Job job) {
    	List passes = super.passes(job);
    	List newPasses = new LinkedList();

    	/*
    	 * Dependent advice
    	 * 
    	 * First we find the names and (ambiguous) Formals of all 
    	 * dependent advice, and store names and Formals
    	 * in the declaring aspect type.
    	 */    	
    	newPasses.add(new VisitorPass(ADVICE_NAMES,
  			  job,
  			  new AdviceNames(job,
  			          				(DATypeSystem) ts, 
  			          				(DANodeFactory)nf)));
    	
    	/*
    	 * Dependent advice
    	 * 
    	 * Then, for all advice dependency declarations where no parameters
    	 * are explicitly given, we set default parameters from the corresponding
    	 * dependent advice declaration.
    	 */    	
        newPasses.add(new VisitorPass(PUSH_PARAM_NAMES,
    			  job,
    			  new PushParamNames(job,
    			          				(DATypeSystem) ts, 
    			          				(DANodeFactory)nf)));

    	/*
    	 * Dependent advice
    	 * 
    	 * Next, type-check the parameter names. For all parameters with the
    	 * same parameter name, we check that the corresponding advice formals
    	 * actually have a common ancestor type.
    	 */    	
        newPasses.add(new VisitorPass(TYPECHECK_PARAM_NAMES,
  			  job,
  			  new TypeCheckAdviceParams(job,
  			          				(DATypeSystem) ts, 
  			          				(DANodeFactory)nf)));

    	/*
    	 * Dependent advice
    	 * 
    	 * Now find dependent advice that are "orphan", i.e. are never referenced
    	 * in a dependency declaration. Throw an error if one is found.
    	 */    	
        newPasses.add(new VisitorPass(FIND_ORPHAN_DEPENDENT_ADVICE,
    			  job,
    			  new OrphanDependentAdviceFinder(job,
    			          				(DATypeSystem) ts, 
    			          				(DANodeFactory)nf)));

    	afterPass(passes, Pass.DISAM_ALL, newPasses);

    	newPasses = new LinkedList();

    	/*
    	 * Dependent advice
    	 * 
    	 * Register dependent advice and their names with GlobalAspectInfo.
    	 */    	
        newPasses.add(new VisitorPass(HARVEST_DA_ASPECT_INFO,
    			  job,
    			  new DAAspectInfoHarvester(job,
	          				(DATypeSystem) ts, 
	          				(DANodeFactory)nf)));
    	
        afterPass(passes, HARVEST_ASPECT_INFO, newPasses);
        
		return passes;
    }
}

/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abc.main.Debug;
import abc.ra.ast.RANodeFactory;
import abc.ra.ast.RANodeFactory_c;
import abc.ra.parse.Grm;
import abc.ra.types.RATypeSystem_c;
import abc.ra.visit.AddFieldsAndMethods;
import abc.ra.visit.GenerateNormalTMFromRelationalTM;
import abc.ra.visit.GenerateTracematch;

/**
 * Extension info for relational aspects extension.
 * @author Eric Bodden
 */
public class ExtensionInfo extends abc.tm.ExtensionInfo
{
    public static final Pass.ID ADD_FIELDS_AND_METHODS = new Pass.ID("add-fields-and-methods-for-relational-aspects");
    public static final Pass.ID GENERATE_TRACEMATCH = new Pass.ID("generate-tracematch");
    public static final Pass.ID GENERATE_TRACEMATCH_FROM_RELATIONAL_TRACEMATCH = new Pass.ID("generate-tracematch-from-relational-tracematch");

    public static final Flags RELATIONAL_MODIFIER = Flags.createFlag("relational", null);
    
    static {
        // force Topics to load;
        // do not change
        new Topics();
    }

    public ExtensionInfo(Collection jar_classes, Collection source_files)
    {
        super(jar_classes, source_files);
        Debug.v().generateLeakWarnings = false;
    }

    /**
     * {@inheritDoc}
     */
    public String compilerName() {
        return "rel-aspect-compiler";
    }

    /**
     * {@inheritDoc}
     */
    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        //create a lexer for the file; often the standard lexer suffices
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        //this should be the grammar you specified
        Grm grm = new Grm(lexer, ts, nf, eq);
        //and finally the parser; you usually are fine with this one
        return new CupParser(grm, source, eq);
    }

    /**
     * {@inheritDoc}
     */
    protected NodeFactory createNodeFactory() {
        return new RANodeFactory_c();
    }
    
    /**
     * {@inheritDoc}
     */
    protected TypeSystem createTypeSystem() {
    	return new RATypeSystem_c();
    }

    /**
     * Adds visitors for relational aspects.
     */
    public List passes(Job job) {
        List passes = super.passes(job);
        List<VisitorPass> newPasses = new ArrayList<VisitorPass>(2);
        //insert pass after parsing pass; this pass adds to each relational aspect
        //a field declaration for each formal of that aspect, as well as associate/release methods
        newPasses.add(new VisitorPass(ADD_FIELDS_AND_METHODS,job,new AddFieldsAndMethods(nf,ts)));
      //insert another pass, generating a relational tracematch declaration for each relational advice declaration
        newPasses.add(new VisitorPass(GENERATE_TRACEMATCH,job,new GenerateTracematch((RANodeFactory)nf,ts)));
        //insert another pass, generating a nomal tracematch for each relational tracematch declaration
        newPasses.add(new VisitorPass(GENERATE_TRACEMATCH_FROM_RELATIONAL_TRACEMATCH,job,new GenerateNormalTMFromRelationalTM((RANodeFactory)nf,ts)));
        afterPass(
        		passes,
        		Pass.PARSE,
        		newPasses
        );
        
//        //replace original InitChecker by our own implementation
//        replacePass(passes,
//        		Pass.INIT_CHECK,
//        		new SingletonList(
//        				new VisitorPass(Pass.INIT_CHECK,job,new RAInitChecker(job, ts, nf)))
//        		);
        return passes;
    }

}

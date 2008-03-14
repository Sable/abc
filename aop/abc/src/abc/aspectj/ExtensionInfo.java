/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.BarrierPass;
import polyglot.frontend.CupParser;
import polyglot.frontend.EmptyPass;
import polyglot.frontend.FileSource;
import polyglot.frontend.GlobalBarrierPass;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.ParserPass;
import polyglot.frontend.Pass;
import polyglot.frontend.Source;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.visit.AddMemberVisitor;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ConstructorCallChecker;
import polyglot.visit.ExitChecker;
import polyglot.visit.FwdReferenceChecker;
import polyglot.visit.InitChecker;
import polyglot.visit.ReachChecker;
import polyglot.visit.TypeChecker;
import soot.javaToJimple.AnonConstructorFinder;
import soot.javaToJimple.CastInsertionVisitor;
import soot.javaToJimple.SaveASTVisitor;
import soot.javaToJimple.StrictFPPropagator;
import abc.aspectj.ast.AJNodeFactory_c;
import abc.aspectj.parse.Grm;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.types.AJTypeSystem_c;
import abc.aspectj.visit.AJAmbiguityRemover;
import abc.aspectj.visit.AJTypeBuilder;
import abc.aspectj.visit.AnonBodyITDs;
import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.AspectNameCollector;
import abc.aspectj.visit.AspectReflectionInspect;
import abc.aspectj.visit.AspectReflectionRewrite;
import abc.aspectj.visit.CheckPackageNames;
import abc.aspectj.visit.CleanAspectMembers;
import abc.aspectj.visit.CollectJimplifyVisitor;
import abc.aspectj.visit.ComputePrecedenceRelation;
import abc.aspectj.visit.DeclareParentsAmbiguityRemover;
import abc.aspectj.visit.DependsChecker;
import abc.aspectj.visit.HierarchyBuilder;
import abc.aspectj.visit.InitClasses;
import abc.aspectj.visit.InterfaceITDs;
import abc.aspectj.visit.JarCheck;
import abc.aspectj.visit.Jimplify;
import abc.aspectj.visit.MangleNameComponents;
import abc.aspectj.visit.MangleNames;
import abc.aspectj.visit.NamePatternEvaluator;
import abc.aspectj.visit.NamePatternReevaluator;
import abc.aspectj.visit.PCStructure;
import abc.aspectj.visit.ParentDeclarer;
import abc.aspectj.visit.PatternMatcher;
import abc.aspectj.visit.PatternTester;
import abc.aspectj.visit.SourceClasses;
import abc.main.AbcTimer;

/**
 * Extension information for aspectj extension.
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 * @author Julian Tibble
 */
public class ExtensionInfo extends soot.javaToJimple.jj.ExtensionInfo {

    public static final polyglot.frontend.Pass.ID COLLECT_SOURCE_FILES = new polyglot.frontend.Pass.ID("collect-source-files");
    public static final polyglot.frontend.Pass.ID CHECK_PACKAGE_NAMES = new polyglot.frontend.Pass.ID("check-package-names");

    public static final polyglot.frontend.Pass.ID INIT_CLASSES = new polyglot.frontend.Pass.ID("init-classes");

    public static final polyglot.frontend.Pass.ID CHECKING_DONE = new polyglot.frontend.Pass.ID("checking-done");
    public static final polyglot.frontend.Pass.ID ASPECT_METHODS = new polyglot.frontend.Pass.ID("aspect-methods");
    public static final polyglot.frontend.Pass.ID INSPECT_AST = new polyglot.frontend.Pass.ID("inspect-ast");
	
	public static final polyglot.frontend.Pass.ID CLEAN_CLASSES = new polyglot.frontend.Pass.ID("clean-classes");
    public static final polyglot.frontend.Pass.ID COLLECT_ASPECT_NAMES = new polyglot.frontend.Pass.ID("collect-aspect-names");
    public static final polyglot.frontend.Pass.ID BUILD_HIERARCHY = new polyglot.frontend.Pass.ID("build-hierarchy");
    public static final polyglot.frontend.Pass.ID HIERARCHY_BUILT = new polyglot.frontend.Pass.ID("hierarchy-built");
    public static final polyglot.frontend.Pass.ID EVALUATE_PATTERNS = new polyglot.frontend.Pass.ID("evaluate-patterns");
    public static final polyglot.frontend.Pass.ID PATTERNS_EVALUATED = new polyglot.frontend.Pass.ID("patterns-evaluated");
    public static final polyglot.frontend.Pass.ID TEST_PATTERNS = new polyglot.frontend.Pass.ID("test-patterns");
    public static final polyglot.frontend.Pass.ID DECLARE_PARENTS = new polyglot.frontend.Pass.ID("declare-parents");
    public static final polyglot.frontend.Pass.ID PARENTS_DECLARED = new polyglot.frontend.Pass.ID("parents-declared");

    public static final polyglot.frontend.Pass.ID EVALUATE_PATTERNS_AGAIN = new polyglot.frontend.Pass.ID("evaluate-patterns-again");
    public static final polyglot.frontend.Pass.ID PATTERNS_EVALUATED_AGAIN = new polyglot.frontend.Pass.ID("patterns-evaluated-again");

    public static final polyglot.frontend.Pass.ID COMPUTE_PRECEDENCE_RELATION = new polyglot.frontend.Pass.ID("compute-precedence-relation");
    public static final polyglot.frontend.Pass.ID PRECEDENCE_COMPUTED = new polyglot.frontend.Pass.ID("precedence-computed");
   
    public static final polyglot.frontend.Pass.ID INTERFACE_ITDS = new polyglot.frontend.Pass.ID("interface-itds");
    public static final polyglot.frontend.Pass.ID ANON_ITDS = new polyglot.frontend.Pass.ID("anon-itds");
    public static final polyglot.frontend.Pass.ID SOURCE_CLASSES = new polyglot.frontend.Pass.ID("source-classes");
    public static final polyglot.frontend.Pass.ID INTERFACE_ITDS_ALL = new polyglot.frontend.Pass.ID("interface-itds-all");
	public static final polyglot.frontend.Pass.ID JAR_CHECK = new polyglot.frontend.Pass.ID("jar-check");
	public static final polyglot.frontend.Pass.ID SET_DEPENDS = new polyglot.frontend.Pass.ID("set-depends");
	public static final polyglot.frontend.Pass.ID CHECK_DEPENDS = new polyglot.frontend.Pass.ID("check-depends");
	
	public static final polyglot.frontend.Pass.ID MANGLE_NAME_COMPONENTS = new polyglot.frontend.Pass.ID("mangle-name-components");
	public static final polyglot.frontend.Pass.ID NAME_COMPONENTS = new polyglot.frontend.Pass.ID("name-components");
    public static final polyglot.frontend.Pass.ID MANGLE_NAMES = new polyglot.frontend.Pass.ID("mangle-names");
	public static final polyglot.frontend.Pass.ID NAMES_MANGLED = new polyglot.frontend.Pass.ID("names-mangled");
	
    public static final polyglot.frontend.Pass.ID CLEAN_DECLARE = new polyglot.frontend.Pass.ID("clean-declare");
    public static final polyglot.frontend.Pass.ID CAST_INSERTION = new polyglot.frontend.Pass.ID("cast-insertion");
    public static final polyglot.frontend.Pass.ID ANON_CONSTR_FINDER = new polyglot.frontend.Pass.ID("anon-constr-finder");
    public static final polyglot.frontend.Pass.ID STRICTFP_PROP = new polyglot.frontend.Pass.ID("strictfp-prop");
    public static final polyglot.frontend.Pass.ID SAVE_AST = new polyglot.frontend.Pass.ID("save-ast");

	public static final polyglot.frontend.Pass.ID ASPECT_PREPARE = new polyglot.frontend.Pass.ID("aspect-prepare");
    public static final polyglot.frontend.Pass.ID HARVEST_ASPECT_INFO = new polyglot.frontend.Pass.ID("harvest");
    public static final polyglot.frontend.Pass.ID CLEAN_MEMBERS = new polyglot.frontend.Pass.ID("clean-members");

    public static final polyglot.frontend.Pass.ID COLLECT_JIMPLIFY_CLASSES = new polyglot.frontend.Pass.ID("collect-jimplify");
    public static final polyglot.frontend.Pass.ID GOING_TO_JIMPLIFY = new polyglot.frontend.Pass.ID("going-to-jimplify");
    public static final polyglot.frontend.Pass.ID JIMPLIFY = new polyglot.frontend.Pass.ID("jimplify");
    public static final polyglot.frontend.Pass.ID JIMPLIFY_DONE = new polyglot.frontend.Pass.ID("jimplify-done");
    public static final polyglot.frontend.Pass.ID EVALUATE_PATTERNS_FINALLY = new polyglot.frontend.Pass.ID("evaluate-patterns-finally");

    public static final polyglot.frontend.Pass.ID ASPECT_REFLECTION_INSPECT = new polyglot.frontend.Pass.ID("aspect-reflection-inspect");
    public static final polyglot.frontend.Pass.ID ASPECT_REFLECTION_REWRITE = new polyglot.frontend.Pass.ID("aspect-reflection-rewrite");
    

    /** The JVM names for all classes loaded from jar files */
    public Collection<String> jar_classes;

    public Collection<String> source_files;
    public Map<String,Node> class_to_ast;
    public PCStructure hierarchy;
    public PatternMatcher pattern_matcher;
    public Collection<String> aspect_names;
    public Map<String,Set<String>> prec_rel = new HashMap<String,Set<String>>();

    public ExtensionInfo(Collection<String> jar_classes, Collection<String> source_files)  {
	this.jar_classes = jar_classes;
	this.source_files = source_files;
	class_to_ast = new HashMap<String,Node>();
	aspect_names = new ArrayList<String>();
	hierarchy = PCStructure.v();
    }

    static {
        // force Topics to load
        new Topics();
    }

    public String defaultFileExtension() {
        return "java";
    }

    public String[] defaultFileExtensions() {
       String [] extnames = { "aj","java" };
       return extnames;
    }

    public String compilerName() {
        return "abc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new AJNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new AJTypeSystem_c();
    }

    public void addDependencyToCurrentJob(Source s) {
	// Ignore nonexisting jobs
	try {
	    super.addDependencyToCurrentJob(s);
	} catch (InternalCompilerError e) {}
    }

    public List<Pass> passes(Job job) {
        ArrayList<Pass> l = new ArrayList<Pass>(25);
        l.add(new InitClasses(INIT_CLASSES, this, ts));

        passes_parse_and_clean(l, job);
        passes_patterns_and_parents(l, job);
        passes_precedence_relation(l, job);
        passes_disambiguate_signatures(l, job);
        passes_add_members(l, job);
        passes_interface_ITDs(l, job);
        passes_disambiguate_all(l, job); 
        passes_fold_and_checkcode(l, job); 
        passes_saveAST(l, job);
        passes_mangle_names(l, job);
        passes_aspectj_transforms(l, job);
        passes_jimple(l, job);

        // re-evaluate patterns for Soot classes; previously only for weavable classes
        // but soot classes are not loaded here: FIXME: move call to main
        //l.add(new NamePatternReevaluator(EVALUATE_PATTERNS_FINALLY));

        // no class serialization, because abc does not support incremental/separate compilation
        // of aspects and aspect-aware classes.
        
        // l.add(new OutputPass(Pass.OUTPUT, job, new Translator(job, ts, nf, targetFactory())));

        // grab this list for the timing module
        AbcTimer.storePolyglotPasses(l); 
        return l;
    }

    protected void passes_parse_and_clean(List<Pass> l, Job job)
    {
        l.add(new ParserPass(Pass.PARSE,compiler,job));

        l.add(new VisitorPass(Pass.BUILD_TYPES, job, new AJTypeBuilder(job, ts, nf)));

        l.add(new GlobalBarrierPass(Pass.BUILD_TYPES_ALL, job));
        l.add(new VisitorPass(Pass.CLEAN_SUPER, job,
                             new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SUPER)));
        l.add(new BarrierPass(Pass.CLEAN_SUPER_ALL, job));
       
    }

    protected void passes_patterns_and_parents(List<Pass> l, Job job)
    {
    	// Disambiguate inner/outer classes
		l.add(new VisitorPass(CLEAN_CLASSES,job, new AJAmbiguityRemover(job,ts,nf,AmbiguityRemover.SIGNATURES)));
        // Disambiguate parents in declare parents
        l.add(new VisitorPass(CLEAN_DECLARE, job,
                              new DeclareParentsAmbiguityRemover(job, ts, nf))); 
        // Collect the full names of aspects
        l.add(new VisitorPass(COLLECT_ASPECT_NAMES, job, new AspectNameCollector(aspect_names)));
        // Build the internal hierarchy/package structure for the pattern matcher
        // The list of weavable classes is available after this pass
        l.add(new VisitorPass(BUILD_HIERARCHY, job, new HierarchyBuilder(this)));
        l.add(new GlobalBarrierPass(HIERARCHY_BUILT, job));
		// Check that packages match directories
	    l.add(new CheckPackageNames(CHECK_PACKAGE_NAMES,job));
	    // Finds list of classes matched by each name pattern
        l.add(new VisitorPass(EVALUATE_PATTERNS, job, new NamePatternEvaluator(this)));
        if (abc.main.Debug.v().namePatternMatches) {
            // Print list of matched classes for each name pattern
            l.add(new VisitorPass(TEST_PATTERNS, job, new PatternTester(this)));
        }
        l.add(new GlobalBarrierPass(PATTERNS_EVALUATED, job));
        // Alter hierarchy according to declare parents (both internal pattern matching hierarchy
        // and Polyglot hierarchy)
        l.add(new VisitorPass(DECLARE_PARENTS, job, new ParentDeclarer(job, ts, nf, this)));
        l.add(new GlobalBarrierPass(PARENTS_DECLARED, job));
        // Finds list of classes matched by each name pattern, according to new hierarchy
        // but only for weavable classes.
        l.add(new NamePatternReevaluator(EVALUATE_PATTERNS_AGAIN));
        l.add(new GlobalBarrierPass(PATTERNS_EVALUATED_AGAIN, job));
    }

    protected void passes_precedence_relation(List<Pass> l, Job job)
    {
    	 // compute precedence relation between aspects, based on matched name patterns
        l.add(new VisitorPass(COMPUTE_PRECEDENCE_RELATION, job, new ComputePrecedenceRelation(job, ts, nf, this)));
        l.add(new GlobalBarrierPass(PRECEDENCE_COMPUTED, job));
    }

    protected void passes_fold_and_checkcode(List<Pass> l, Job job)
    {
    	// constant folder. FIXME: this folds bytes to ints
        //l.add(new VisitorPass(Pass.FOLD, job, new ConstantFolder(ts, nf)));
        // typechecker
        l.add(new VisitorPass(Pass.TYPE_CHECK, job, new TypeChecker(job, ts, nf)));
        // reachability checker
        l.add(new VisitorPass(Pass.REACH_CHECK, job, new ReachChecker(job, ts, nf)));
        // Exceptions are now checked after weaving, because of softening
        // l.add(new VisitorPass(Pass.EXC_CHECK, job, new ExceptionChecker(job,ts,nf)));
        // insert casts for e.g. byte to int (j2j)
        l.add(new VisitorPass(CAST_INSERTION, job, new CastInsertionVisitor(job, ts, nf)));
        l.add(new VisitorPass(ANON_CONSTR_FINDER, job, new AnonConstructorFinder(job, ts, nf)));
        // strictfp modifier is propagated to all textually enclosed members
        l.add(new VisitorPass(STRICTFP_PROP, job, new StrictFPPropagator(false)));
        // definite return checks
        l.add(new VisitorPass(Pass.EXIT_CHECK, job, new ExitChecker(job, ts, nf)));
        // definite initialization
        l.add(new VisitorPass(Pass.INIT_CHECK, job, new InitChecker(job, ts, nf)));
        // ccalls are not recursive
        l.add(new VisitorPass(Pass.CONSTRUCTOR_CHECK, job, new ConstructorCallChecker(job, ts, nf)));
        // order of field inits
        l.add(new VisitorPass(Pass.FWD_REF_CHECK, job, new FwdReferenceChecker(job, ts, nf)));
	    
	    // check for itd conflicts in jars
        l.add(new JarCheck(JAR_CHECK,job,ts));
        
        l.add(new GlobalBarrierPass(SET_DEPENDS,job));
        // pointcuts are not recursive; and concrete pointcuts don't depend on abstract ones
		l.add(new VisitorPass(CHECK_DEPENDS,job, new DependsChecker(job,ts,nf)));
		
        l.add(new GlobalBarrierPass(CHECKING_DONE, job));
        
    }

    protected void passes_saveAST(List<Pass> l, Job job)
    {
	l.add(new EmptyPass(Pass.PRE_OUTPUT_ALL));
	// tell soot the connection between source and job, so it doesn't re-compile
	l.add(new SaveASTVisitor(SAVE_AST, job, this));
    }

    protected void passes_mangle_names(List<Pass> l, Job job)
    {
    	// determine components that need to get the same mangled name
    l.add(new VisitorPass(MANGLE_NAME_COMPONENTS, job, new MangleNameComponents()));
    l.add(new GlobalBarrierPass(NAME_COMPONENTS, job));
    // record what mangled names will be
	l.add(new VisitorPass(MANGLE_NAMES, job, new MangleNames()));
	l.add(new GlobalBarrierPass(NAMES_MANGLED, job));
    }

    protected void passes_aspectj_transforms(List<Pass> l, Job job)
    {
	// look to see if all thisJoinPoint references could be changed into thisJoinPointStaticPart
	l.add(new VisitorPass(ASPECT_REFLECTION_INSPECT,job, new AspectReflectionInspect()));
	// Change them
	l.add(new VisitorPass(ASPECT_REFLECTION_REWRITE,job, new AspectReflectionRewrite(nf,ts)));

        // add new methods for proceed and if-pointcuts, and turn advice into methods
        // mangle names, introduce accessor methods
        l.add(new VisitorPass(ASPECT_METHODS,job, new AspectMethods(job,nf,ts)));
        l.add(new GlobalBarrierPass(ASPECT_PREPARE,job));
        
        // to test the above:
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
        // build AspectInfo structure, register real names and real classes
        l.add(new VisitorPass(HARVEST_ASPECT_INFO, job, new AspectInfoHarvester(job, ts, nf)));
        // remove all AST nodes and members of classtypes that will be re-introduced by weaving
        l.add(new VisitorPass(CLEAN_MEMBERS, job, new CleanAspectMembers(nf,ts)));
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
    }

    protected void passes_jimple(List<Pass> l, Job job)
    {
    	// set up map from top-level classes to asts for j2j
        l.add(new VisitorPass(COLLECT_JIMPLIFY_CLASSES, job,
                              new CollectJimplifyVisitor(job, ts, nf, source_files, class_to_ast)));
        l.add(new GlobalBarrierPass(GOING_TO_JIMPLIFY, job));
        l.add(new Jimplify(JIMPLIFY, class_to_ast));
        l.add(new GlobalBarrierPass(JIMPLIFY_DONE, job));
    }

    protected void passes_disambiguate_signatures(List<Pass> l, Job job)
    {
    	// disambiguate inner/outer classes, signatures of methods
        l.add(new VisitorPass(Pass.CLEAN_SIGS, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SIGNATURES)));
    }

    protected void passes_add_members(List<Pass> l, Job job)
    {
    	// populate class types with members
        l.add(new VisitorPass(Pass.ADD_MEMBERS, job, new AddMemberVisitor(job, ts, nf)));
        l.add(new GlobalBarrierPass(Pass.ADD_MEMBERS_ALL, job));
    }

    protected void passes_interface_ITDs(List<Pass> l, Job job)
    {
    	// put interface itds in types
        l.add(new InterfaceITDs(INTERFACE_ITDS));
        // collect all class types that came from source files (in preparation for typechecking classes from classfiles)
        l.add(new VisitorPass(SOURCE_CLASSES, job, new SourceClasses()));
        // add interface itds to anonymous classes - interfaces for these are added separately in a locally
        // spawned pass
        l.add(new VisitorPass(ANON_ITDS,job,new AnonBodyITDs(job,ts,nf)));
        l.add(new GlobalBarrierPass(INTERFACE_ITDS_ALL,job));
    }

    protected void passes_disambiguate_all(List<Pass> l, Job job)
    {
    	// resolve all variable, class references
        l.add(new VisitorPass(Pass.DISAM, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL)));
        l.add(new BarrierPass(Pass.DISAM_ALL, job));
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
    }
}

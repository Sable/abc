package abc.aspectj;

import polyglot.lex.Lexer;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.parse.Grm;
import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import abc.main.AbcTimer;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.*;

import soot.javaToJimple.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for aspectj extension.
 */
public class ExtensionInfo extends soot.javaToJimple.jj.ExtensionInfo {

    public static final polyglot.frontend.Pass.ID COLLECT_SOURCE_FILES = new polyglot.frontend.Pass.ID("collect-source-files");
    public static final polyglot.frontend.Pass.ID CHECK_PACKAGE_NAMES = new polyglot.frontend.Pass.ID("check-package-names");

    public static final polyglot.frontend.Pass.ID INIT_CLASSES = new polyglot.frontend.Pass.ID("init-classes");

    public static final polyglot.frontend.Pass.ID CHECKING_DONE = new polyglot.frontend.Pass.ID("checking-done");
    public static final polyglot.frontend.Pass.ID ASPECT_METHODS = new polyglot.frontend.Pass.ID("aspect-methods");
    public static final polyglot.frontend.Pass.ID INSPECT_AST = new polyglot.frontend.Pass.ID("inspect-ast");
	
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
    public static final polyglot.frontend.Pass.ID SOURCE_CLASSES = new polyglot.frontend.Pass.ID("source-classes");
    public static final polyglot.frontend.Pass.ID INTERFACE_ITDS_ALL = new polyglot.frontend.Pass.ID("interface-itds-all");
	public static final polyglot.frontend.Pass.ID JAR_CHECK = new polyglot.frontend.Pass.ID("jar-check");
	public static final polyglot.frontend.Pass.ID SET_DEPENDS = new polyglot.frontend.Pass.ID("set-depends");
	public static final polyglot.frontend.Pass.ID CHECK_DEPENDS = new polyglot.frontend.Pass.ID("check-depends");
	
    public static final polyglot.frontend.Pass.ID MANGLE_NAMES = new polyglot.frontend.Pass.ID("mangle-names");
	public static final polyglot.frontend.Pass.ID NAMES_MANGLED = new polyglot.frontend.Pass.ID("names-mangled");
	
    public static final polyglot.frontend.Pass.ID CLEAN_DECLARE = new polyglot.frontend.Pass.ID("clean-declare");
    public static final polyglot.frontend.Pass.ID CAST_INSERTION = new polyglot.frontend.Pass.ID("cast-insertion");
    public static final polyglot.frontend.Pass.ID SAVE_AST = new polyglot.frontend.Pass.ID("save-ast");

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
    public Collection/*<String>*/ jar_classes;

    public Collection/*<String>*/ source_files;
    public Map/*<String,Node>*/ class_to_ast;
    public PCStructure hierarchy;
    public PatternMatcher pattern_matcher;
    public Collection/*<String>*/ aspect_names;
    public Map/*<String,Set<String>>*/ prec_rel = new HashMap();

    public ExtensionInfo(Collection jar_classes, Collection source_files)  {
	this.jar_classes = jar_classes;
	this.source_files = source_files;
	class_to_ast = new HashMap();
	aspect_names = new ArrayList();
	hierarchy = PCStructure.v();
    }

    static {
        // force Topics to load
        Topics t = new Topics();
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
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
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

    public List passes(Job job) {
        ArrayList l = new ArrayList(25);
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

        l.add(new NamePatternReevaluator(EVALUATE_PATTERNS_AGAIN));

        if (compiler.serializeClassInfo()) {
            l.add(new VisitorPass(Pass.SERIALIZE,
                                  job, new ClassSerializer(ts, nf,
                                                           job.source().lastModified(),
                                                           compiler.errorQueue(),
                                                           version())));
        }

        // l.add(new OutputPass(Pass.OUTPUT, job, new Translator(job, ts, nf, targetFactory())));

        // grab this list for the timing module
        AbcTimer.storePolyglotPasses(l); 
        return l;
    }

    protected void passes_parse_and_clean(List l, Job job)
    {
        l.add(new ParserPass(Pass.PARSE,compiler,job));

        l.add(new VisitorPass(Pass.BUILD_TYPES, job, new AJTypeBuilder(job, ts, nf)));

        l.add(new GlobalBarrierPass(Pass.BUILD_TYPES_ALL, job));
        l.add(new VisitorPass(Pass.CLEAN_SUPER, job,
                             new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SUPER)));
        l.add(new BarrierPass(Pass.CLEAN_SUPER_ALL, job));
    }

    protected void passes_patterns_and_parents(List l, Job job)
    {
        // Pattern and declare parents stuff
        l.add(new VisitorPass(CLEAN_DECLARE, job,
                              new DeclareParentsAmbiguityRemover(job, ts, nf)));
        l.add(new VisitorPass(COLLECT_ASPECT_NAMES, job, new AspectNameCollector(aspect_names)));
        l.add(new VisitorPass(BUILD_HIERARCHY, job, new HierarchyBuilder(this)));
        l.add(new GlobalBarrierPass(HIERARCHY_BUILT, job));

	l.add(new CheckPackageNames(CHECK_PACKAGE_NAMES,job));
        l.add(new VisitorPass(EVALUATE_PATTERNS, job, new NamePatternEvaluator(this)));
        if (abc.main.Debug.v().namePatternMatches) {
            l.add(new VisitorPass(TEST_PATTERNS, job, new PatternTester(this)));
        }
        l.add(new GlobalBarrierPass(PATTERNS_EVALUATED, job));
        l.add(new VisitorPass(DECLARE_PARENTS, job, new ParentDeclarer(job, ts, nf, this)));
        l.add(new GlobalBarrierPass(PARENTS_DECLARED, job));
        l.add(new NamePatternReevaluator(EVALUATE_PATTERNS_AGAIN));
        l.add(new GlobalBarrierPass(PATTERNS_EVALUATED_AGAIN, job));
    }

    protected void passes_precedence_relation(List l, Job job)
    {
        l.add(new VisitorPass(COMPUTE_PRECEDENCE_RELATION, job, new ComputePrecedenceRelation(job, ts, nf, this)));
        l.add(new GlobalBarrierPass(PRECEDENCE_COMPUTED, job));
    }

    protected void passes_fold_and_checkcode(List l, Job job)
    {
        l.add(new VisitorPass(Pass.FOLD, job, new ConstantFolder(ts, nf)));
        l.add(new VisitorPass(Pass.TYPE_CHECK, job, new TypeChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.REACH_CHECK, job, new ReachChecker(job, ts, nf)));
        // Exceptions are now checked after weaving, because of softening
        // l.add(new VisitorPass(Pass.EXC_CHECK, job, new ExceptionChecker(job,ts,nf)));
        l.add(new VisitorPass(CAST_INSERTION, job, new CastInsertionVisitor(job, ts, nf)));
        l.add(new VisitorPass(Pass.EXIT_CHECK, job, new ExitChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.INIT_CHECK, job, new InitChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.CONSTRUCTOR_CHECK, job, new ConstructorCallChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.FWD_REF_CHECK, job, new FwdReferenceChecker(job, ts, nf)));
	
        l.add(new JarCheck(JAR_CHECK,job,ts));
        
        l.add(new GlobalBarrierPass(SET_DEPENDS,job));
		l.add(new VisitorPass(CHECK_DEPENDS,job, new DependsChecker(job,ts,nf)));
		
        l.add(new GlobalBarrierPass(CHECKING_DONE, job));
        
    }

    protected void passes_saveAST(List l, Job job)
    {
	l.add(new EmptyPass(Pass.PRE_OUTPUT_ALL));
	l.add(new SaveASTVisitor(SAVE_AST, job, this));
    }

    protected void passes_mangle_names(List l, Job job)
    {
	l.add(new VisitorPass(MANGLE_NAMES, job, new MangleNames(ts)));
	l.add(new GlobalBarrierPass(NAMES_MANGLED, job));
    }

    protected void passes_aspectj_transforms(List l, Job job)
    {
	// look to see if all thisJoinPoint references could be changed into thisJoinPointStaticPart
	l.add(new VisitorPass(ASPECT_REFLECTION_INSPECT,job, new AspectReflectionInspect()));
	// Change them
	l.add(new VisitorPass(ASPECT_REFLECTION_REWRITE,job, new AspectReflectionRewrite(nf,ts)));

        // add new methods for proceed and if-pointcuts, and turn advice into methods
        l.add(new VisitorPass(ASPECT_METHODS,job, new AspectMethods(job,nf,ts)));

        // to test the above:
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
        l.add(new VisitorPass(HARVEST_ASPECT_INFO, job, new AspectInfoHarvester(job, ts, nf)));
        l.add(new VisitorPass(CLEAN_MEMBERS, job, new CleanAspectMembers(nf,ts)));
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
    }

    protected void passes_jimple(List l, Job job)
    {
        l.add(new VisitorPass(COLLECT_JIMPLIFY_CLASSES, job,
                              new CollectJimplifyVisitor(job, ts, nf, source_files, class_to_ast)));
        l.add(new GlobalBarrierPass(GOING_TO_JIMPLIFY, job));
        l.add(new Jimplify(JIMPLIFY, class_to_ast));
        l.add(new GlobalBarrierPass(JIMPLIFY_DONE, job));
    }

    protected void passes_disambiguate_signatures(List l, Job job)
    {
        l.add(new VisitorPass(Pass.CLEAN_SIGS, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SIGNATURES)));
    }

    protected void passes_add_members(List l, Job job)
    {
        l.add(new VisitorPass(Pass.ADD_MEMBERS, job, new AddMemberVisitor(job, ts, nf)));
        l.add(new GlobalBarrierPass(Pass.ADD_MEMBERS_ALL, job));
    }

    protected void passes_interface_ITDs(List l, Job job)
    {
        l.add(new InterfaceITDs(INTERFACE_ITDS));
        l.add(new VisitorPass(SOURCE_CLASSES, job, new SourceClasses()));
        l.add(new GlobalBarrierPass(INTERFACE_ITDS_ALL,job));
    }

    protected void passes_disambiguate_all(List l, Job job)
    {
        l.add(new VisitorPass(Pass.DISAM, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL)));
        l.add(new BarrierPass(Pass.DISAM_ALL, job));
        // l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
    }
}

package abc.aspectj;

import polyglot.lex.Lexer;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.parse.Grm;
import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

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

    public static final polyglot.frontend.Pass.ID CHECKING_DONE = new polyglot.frontend.Pass.ID("checking-done");
    public static final polyglot.frontend.Pass.ID ASPECT_METHODS = new polyglot.frontend.Pass.ID("aspect-methods");
    public static final polyglot.frontend.Pass.ID INSPECT_AST = new polyglot.frontend.Pass.ID("inspect-ast");
	
    public static final polyglot.frontend.Pass.ID BUILD_HIERARCHY = new polyglot.frontend.Pass.ID("build-hierarchy");
    public static final polyglot.frontend.Pass.ID HIERARCHY_BUILT = new polyglot.frontend.Pass.ID("hierarchy-built");
    public static final polyglot.frontend.Pass.ID EVALUATE_PATTERNS = new polyglot.frontend.Pass.ID("evaluate-patterns");
    public static final polyglot.frontend.Pass.ID TEST_PATTERNS = new polyglot.frontend.Pass.ID("test-patterns");

    public static final polyglot.frontend.Pass.ID CLEAN_DECLARE = new polyglot.frontend.Pass.ID("clean-declare");
    public static final polyglot.frontend.Pass.ID CAST_INSERTION = new polyglot.frontend.Pass.ID("cast-insertion");
    public static final polyglot.frontend.Pass.ID SAVE_AST = new polyglot.frontend.Pass.ID("save-ast");

    public static final polyglot.frontend.Pass.ID HARVEST_ASPECT_INFO = new polyglot.frontend.Pass.ID("harvest");
    public static final polyglot.frontend.Pass.ID CLEAN_MEMBERS = new polyglot.frontend.Pass.ID("clean-members");

    public static final polyglot.frontend.Pass.ID COLLECT_JIMPLIFY_CLASSES = new polyglot.frontend.Pass.ID("collect-jimplify");
    public static final polyglot.frontend.Pass.ID GOING_TO_JIMPLIFY = new polyglot.frontend.Pass.ID("going-to-jimplify");
    public static final polyglot.frontend.Pass.ID JIMPLIFY = new polyglot.frontend.Pass.ID("jimplify");

    public Collection weavable_classes;
    public Collection jimplify_classes;
    public PCStructure hierarchy;
    public PatternMatcher pattern_matcher;

    public ExtensionInfo(Collection weavable_classes) {
	this.weavable_classes = weavable_classes;
	this.jimplify_classes = new HashSet();
	this.hierarchy = new PCStructure();
	Iterator ci = weavable_classes.iterator();
	while (ci.hasNext()) {
	    String cname = (String)ci.next();
	    hierarchy.insertFullName(cname, true, true);
	}
	this.pattern_matcher = PatternMatcher.create(hierarchy);

    }

    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public String defaultFileExtension() {
        return "java";
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
        return new AspectJNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new AspectJTypeSystem_c();
    }

    public List passes(Job job) {
        ArrayList l = new ArrayList(25);
        l.add(new ParserPass(Pass.PARSE,compiler,job));
        
    
	l.add(new VisitorPass(Pass.BUILD_TYPES, job, new TypeBuilder(job, ts, nf))); 
	l.add(new GlobalBarrierPass(Pass.BUILD_TYPES_ALL, job));
	l.add(new VisitorPass(Pass.CLEAN_SUPER, job,
			      new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SUPER)));
	l.add(new BarrierPass(Pass.CLEAN_SUPER_ALL, job));

	// Pattern and declare parents stuff
	//	l.add(new VisitorPass(CLEAN_DECLARE, job,
        //                      new AmbiguityRemover(job, ts, nf, DeclareParentsAmbiguityRemover.DECLARE)));
	l.add(new VisitorPass(BUILD_HIERARCHY, job, new HierarchyBuilder(hierarchy, weavable_classes)));
	l.add(new GlobalBarrierPass(HIERARCHY_BUILT, job));
	l.add(new VisitorPass(EVALUATE_PATTERNS, job, new NamePatternEvaluator(this)));
	l.add(new VisitorPass(TEST_PATTERNS, job, new PatternTester(this)));
	
	l.add(new VisitorPass(Pass.CLEAN_SIGS, job,
			      new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SIGNATURES)));
    
	l.add(new VisitorPass(Pass.ADD_MEMBERS, job, new AddMemberVisitor(job, ts, nf)));
	l.add(new GlobalBarrierPass(Pass.ADD_MEMBERS_ALL, job));
	
	l.add(new VisitorPass(Pass.DISAM, job,
			      new AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL)));
	l.add(new BarrierPass(Pass.DISAM_ALL, job));
	l.add(new VisitorPass(Pass.FOLD, job, new ConstantFolder(ts, nf)));
        l.add(new VisitorPass(Pass.TYPE_CHECK, job, new TypeChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.REACH_CHECK, job, new ReachChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.EXC_CHECK, job, new ExceptionChecker(ts, compiler.errorQueue())));
	l.add(new VisitorPass(CAST_INSERTION, job, new CastInsertionVisitor(job, ts, nf)));
        l.add(new VisitorPass(Pass.EXIT_CHECK, job, new ExitChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.INIT_CHECK, job, new InitChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.CONSTRUCTOR_CHECK, job, new ConstructorCallChecker(job, ts, nf)));

	l.add(new GlobalBarrierPass(CHECKING_DONE, job));

	l.add(new EmptyPass(Pass.PRE_OUTPUT_ALL));
	l.add(new SaveASTVisitor(SAVE_AST, job, this));
	
	// add new methods for proceed and if-pointcuts, and turn advice into methods
	l.add(new VisitorPass(ASPECT_METHODS,job, new AspectMethods(nf,ts)));

	// to test the above:
	// l.add(new PrettyPrintPass(INSPECT_AST,job,new CodeWriter(System.out,70),new PrettyPrinter()));
        l.add(new VisitorPass(HARVEST_ASPECT_INFO, job, new AspectInfoHarvester(job, ts, nf)));
	l.add(new VisitorPass(CLEAN_MEMBERS, job, new CleanAspectMembers(nf)));
        
	l.add(new VisitorPass(COLLECT_JIMPLIFY_CLASSES, job, new CollectJimplifyVisitor(jimplify_classes)));
	l.add(new GlobalBarrierPass(GOING_TO_JIMPLIFY, job));
	l.add(new VisitorPass(JIMPLIFY, job, new JimplifyVisitor(jimplify_classes)));

	if (compiler.serializeClassInfo()) {
	    l.add(new VisitorPass(Pass.SERIALIZE,
				  job, new ClassSerializer(ts, nf,
							   job.source().lastModified(),
							   compiler.errorQueue(),
                                                           version())));
	}

	 // l.add(new OutputPass(Pass.OUTPUT, job, new Translator(job, ts, nf, targetFactory())));

        return l;
    }

}

package polyglot.ext.aspectj;

import polyglot.lex.Lexer;
import polyglot.ext.aspectj.parse.Lexer_c;
import polyglot.ext.aspectj.parse.Grm;
import polyglot.ext.aspectj.ast.*;
import polyglot.ext.aspectj.types.*;
import polyglot.ext.aspectj.visit.*;

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

    public static final polyglot.frontend.Pass.ID CAST_INSERTION = new polyglot.frontend.Pass.ID("cast-insertion");
    public static final polyglot.frontend.Pass.ID SAVE_AST = new polyglot.frontend.Pass.ID("save-ast");

    public static final polyglot.frontend.Pass.ID JIMPLIFY = new polyglot.frontend.Pass.ID("jimplify");

    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public String defaultFileExtension() {
        return "aj";
    }

    public String compilerName() {
        return "arc";
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
    	System.out.println("type system created!");
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
	l.add(new VisitorPass(Pass.CLEAN_SIGS, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SIGNATURES)));
	l.add(new VisitorPass(Pass.ADD_MEMBERS, job, new AddMemberVisitor(job, ts, nf)));
	l.add(new BarrierPass(Pass.ADD_MEMBERS_ALL, job));
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

	l.add(new EmptyPass(Pass.PRE_OUTPUT_ALL));
	//l.add(new SaveASTVisitor(SAVE_AST, job, this));
	l.add(new VisitorPass(JIMPLIFY, job, new JimplifyVisitor()));

	/*
	if (compiler.serializeClassInfo()) {
	    l.add(new VisitorPass(Pass.SERIALIZE,
				  job, new ClassSerializer(ts, nf,
							   job.source().lastModified(),
							   compiler.errorQueue(),
                                                           version())));
	}
	*/

	//l.add(new OutputPass(Pass.OUTPUT, job, new Translator(job, ts, nf, targetFactory())));

        return l;
    }

}

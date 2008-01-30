/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1;

import java.io.Reader;
import java.util.Collection;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.GlobalBarrierPass;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abcexer1.ast.Abcexer1NodeFactory;
import abcexer1.ast.Abcexer1NodeFactory_c;
import abcexer1.parse.Grm;
import abcexer1.visit.Surround;


/**
 * @author Sascha Kuzins
 *
 */
public class ExtensionInfo extends abc.aspectj.ExtensionInfo {
	public static final Pass.ID TRANSFORM_SURROUND_ADVICE =
        new Pass.ID("transform-surround-advice");
    public static final Pass.ID BEFORE_SURROUND_TRANSFORMATION =
        new Pass.ID("before-surround-transformation");
    public static final Pass.ID AFTER_SURROUND_TRANSFORMATION =
        new Pass.ID("after-surround-transformation");
    
	public ExtensionInfo(Collection jar_classes, Collection source_files)
    {
        super(jar_classes, source_files);
    }
	
	public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }
	
    protected NodeFactory createNodeFactory() {
        return new Abcexer1NodeFactory_c();
    }

    protected void passes_patterns_and_parents(List l, Job job)
    {
    	

    	l.add(new GlobalBarrierPass(BEFORE_SURROUND_TRANSFORMATION, job));
    	 
        l.add(new VisitorPass(TRANSFORM_SURROUND_ADVICE,
                              job,
                              new Surround(
                                                   job,
                                                   ts,
                                                   (Abcexer1NodeFactory) nf)));
        
    	l.add(new GlobalBarrierPass(AFTER_SURROUND_TRANSFORMATION, job));
    	
    	super.passes_patterns_and_parents(l, job);
    	
    }
}

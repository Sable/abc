/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1;

import java.io.Reader;
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
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abcexer1.ast.Abcexer1NodeFactory;
import abcexer1.ast.Abcexer1NodeFactory_c;
import abcexer1.parse.Grm;
import abcexer1.visit.Surround;


/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExtensionInfo extends abc.aspectj.ExtensionInfo {
	public static final Pass.ID TRANSFORM_SURROUND_ADVICE =
        new Pass.ID("transform-surround-advice");
	
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
        

        l.add(new VisitorPass(TRANSFORM_SURROUND_ADVICE,
                              job,
                              new Surround(/*GlobalPointcuts.COLLECT,
                                                   job,
                                                   (EAJTypeSystem) ts,*/
                                                   (Abcexer1NodeFactory) nf)));
        
        super.passes_patterns_and_parents(l, job);
    }
}

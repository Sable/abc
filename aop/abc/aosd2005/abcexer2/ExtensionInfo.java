/*
 * Created on 09-Feb-2005
 *
 */
package abcexer2;

import java.io.Reader;
import java.util.Collection;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.Parser;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abcexer2.ast.Abcexer2NodeFactory_c;
import abcexer2.parse.Grm;
import abcexer2.types.Abcexer2TypeSystem_c;

/**
 * @author Sascha Kuzins
 *
 */
public class ExtensionInfo extends abc.aspectj.ExtensionInfo {

	public ExtensionInfo(Collection jar_classes, Collection source_files) {
		super(jar_classes, source_files);
	}
	
	public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }
	protected NodeFactory createNodeFactory() {
        return new Abcexer2NodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new Abcexer2TypeSystem_c();
    }

}

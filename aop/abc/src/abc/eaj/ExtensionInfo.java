package abc.eaj;

import polyglot.lex.Lexer;
import abc.aspectj.parse.Lexer_c;
import abc.eaj.parse.Grm;
import abc.eaj.ast.*;
import abc.eaj.types.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

import java.util.*;
import java.io.Reader;

/**
 * Extension information for Extended AspectJ extension.
 */
public class ExtensionInfo extends abc.aspectj.ExtensionInfo
{
    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public ExtensionInfo(Collection jar_classes, Collection source_files)
    {
        super(jar_classes, source_files);
    }

    public String compilerName() {
        return "eaj";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new EAJNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new EAJTypeSystem_c();
    }

//    public List passes(Job job) {
//        List passes = super.passes(job);
//        // TODO: add passes as needed by your compiler
//        return passes;
//    }

}

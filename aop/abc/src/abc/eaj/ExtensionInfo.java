package abc.eaj;

import polyglot.lex.Lexer;
import abc.eaj.parse.Lexer_c;
import abc.eaj.parse.Grm;
import abc.eaj.ast.*;
import abc.eaj.types.*;
import abc.eaj.visit.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

import java.util.*;
import java.io.Reader;

/**
 * Extension information for Extended AspectJ extension.
 * @author Julian Tibble
 */
public class ExtensionInfo extends abc.aspectj.ExtensionInfo
{
    public static final Pass.ID COLLECT_GLOBAL_POINTCUTS =
            new Pass.ID("collect-global-pointcuts");
    public static final Pass.ID COLLECTED_GLOBAL_POINTCUTS =
            new Pass.ID("collected-global-pointcuts");
    public static final Pass.ID CONJOIN_GLOBAL_POINTCUTS =
            new Pass.ID("conjoin-global-pointcuts");
    public static final Pass.ID CONJOINED_GLOBAL_POINTCUTS =
            new Pass.ID("conjoined-global-pointcuts");

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

    public List passes(Job job) {
        // FIXME: shouldn't really be here:
        abc.eaj.weaving.matching.CastShadowType.register();

        return super.passes(job);
    }

    protected void passes_patterns_and_parents(List l, Job job)
    {
        super.passes_patterns_and_parents(l, job);

        l.add(new VisitorPass(COLLECT_GLOBAL_POINTCUTS,
                              job,
                              new GlobalPointcuts(GlobalPointcuts.COLLECT,
                                                   job,
                                                   (EAJTypeSystem) ts,
                                                   (EAJNodeFactory) nf)));
        l.add(new GlobalBarrierPass(COLLECTED_GLOBAL_POINTCUTS, job));
        l.add(new VisitorPass(COLLECT_GLOBAL_POINTCUTS,
                              job,
                              new GlobalPointcuts(GlobalPointcuts.CONJOIN,
                                                  job,
                                                  (EAJTypeSystem) ts,
                                                  (EAJNodeFactory) nf)));
    }
}

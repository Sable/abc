/* abc - The AspectBench Compiler
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

package abc.eaj;

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
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abc.eaj.ast.EAJNodeFactory;
import abc.eaj.ast.EAJNodeFactory_c;
import abc.eaj.parse.Grm;
import abc.eaj.types.EAJTypeSystem;
import abc.eaj.types.EAJTypeSystem_c;
import abc.eaj.visit.CheckPCContainsStatic;
import abc.eaj.visit.GlobalPointcuts;

/**
 * Extension information for Extended AspectJ extension.
 * @author Julian Tibble
 * @author Eric Bodden
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

    public static final Pass.ID CHECK_PCCONTAINS_STATIC = 
        	new Pass.ID("check_pccontains_static");
    
    static {
        // force Topics to load
        new Topics();
    }

    public ExtensionInfo(Collection<String> jar_classes, Collection<String> source_files)
    {
        super(jar_classes, source_files);
    }

    public String compilerName() {
        return "eaj";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new EAJNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new EAJTypeSystem_c();
    }

    protected void passes_patterns_and_parents(List<Pass> l, Job job)
    {
        super.passes_patterns_and_parents(l, job);

        l.add(new VisitorPass(COLLECT_GLOBAL_POINTCUTS,
                              job,
                              new GlobalPointcuts(GlobalPointcuts.COLLECT,
                                                   job,
                                                   (EAJTypeSystem) ts,
                                                   (EAJNodeFactory) nf)));
        l.add(new GlobalBarrierPass(COLLECTED_GLOBAL_POINTCUTS, job));
        l.add(new VisitorPass(CONJOIN_GLOBAL_POINTCUTS,
                              job,
                              new GlobalPointcuts(GlobalPointcuts.CONJOIN,
                                                  job,
                                                  (EAJTypeSystem) ts,
                                                  (EAJNodeFactory) nf)));
        l.add(new VisitorPass(CHECK_PCCONTAINS_STATIC,
                			  job,
                			  new CheckPCContainsStatic(job,
                			          				(EAJTypeSystem) ts, 
                			          				(EAJNodeFactory)nf)));
    }
}

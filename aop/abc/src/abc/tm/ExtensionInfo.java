/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Chris Allan
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.parse.Lexer_c;
import abc.eaj.types.EAJTypeSystem;
import abc.tm.ast.TMNodeFactory;
import abc.tm.ast.TMNodeFactory_c;
import abc.tm.parse.Grm;
import abc.tm.types.TMTypeSystem_c;
import abc.tm.visit.MoveTraceMatchMembers;

/**
 * Extension information for TraceMatching extension.
 * @author Chris Allan
 * @author Julian Tibble
 */
public class ExtensionInfo extends abc.eaj.ExtensionInfo
{
    static {
        // force Topics to load
        new Topics();
    }

    public ExtensionInfo(Collection jar_classes, Collection source_files)
    {
        super(jar_classes, source_files);
    }

    public String compilerName() {
        return "abc+tracematching";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
            return new TMNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new TMTypeSystem_c();
    }

    public static final Pass.ID CREATE_TRACEMATCH_ADVICE =
            new Pass.ID("create-tracematch-advice");

    public List passes(Job job) {
        EAJTypeSystem ts = (EAJTypeSystem) this.ts;
        TMNodeFactory nf = (TMNodeFactory) this.nf;

        List passes = super.passes(job);

        List newPasses = new LinkedList();
        newPasses.add(new VisitorPass(CREATE_TRACEMATCH_ADVICE,
                        job, new MoveTraceMatchMembers(job, ts, nf)));
//        newPasses.add(new PrettyPrintPass(INSPECT_AST, job,
//                                          new CodeWriter(System.out, 70),
//                                          new PrettyPrinter()));

        afterPass(passes, Pass.DISAM_ALL, newPasses);

        return passes;
    }
}

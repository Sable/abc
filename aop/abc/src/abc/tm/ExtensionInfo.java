/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Chris Allan
 * Copyright (C) 2005 Julian Tibble
 * File based on abc.eaj.ExtensionInfo
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

import polyglot.lex.Lexer;
import abc.aspectj.parse.Lexer_c;
import abc.tm.parse.Grm;

import abc.tm.ast.*;
import abc.tm.visit.*;

import abc.aspectj.types.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.util.ErrorQueue;

import java.util.*;
import java.io.Reader;

/**
 * Extension information for TraceMatching extension.
 * @author Chris Allan
 * @author Julian Tibble
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
        return "abc+tracematching";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new TMNodeFactory_c();
    }

    public static final Pass.ID CREATE_TRACEMATCH_ADVICE =
            new Pass.ID("create-tracematch-advice");

    public List passes(Job job) {
        AJTypeSystem ts = (AJTypeSystem) this.ts;
        TMNodeFactory nf = (TMNodeFactory) this.nf;

        List passes = super.passes(job);

        List newPasses = new LinkedList();
        newPasses.add(new VisitorPass(CREATE_TRACEMATCH_ADVICE,
                        job, new MoveTraceMatchMembers(job, ts, nf)));

        afterPass(passes, Pass.DISAM_ALL, newPasses);

        return passes;
    }
}

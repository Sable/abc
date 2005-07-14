/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.aspectj.visit;

import java.io.File;
import java.util.Iterator;

import polyglot.frontend.*;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

/** Check that public classes are declared in an appropriate
 *  file. This checks both the name of the file itself and that
 *  it has been specified in a path that corresponds to the
 *  package name.
 *  @author Ganesh Sittampalam
 */
public class CheckPackageNames extends OncePass {
    protected Job job;

    public CheckPackageNames(Pass.ID id, Job job) {
        super(id);
        this.job=job;
    }

    public void once() {
        for (Iterator weavableClasses = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator();
             weavableClasses.hasNext(); ) {
            ClassType ctype = ((AbcClass) weavableClasses.next()).getPolyglotType();

            if(!(ctype instanceof ParsedClassType)) continue;

            ParsedClassType pctype = (ParsedClassType) ctype;

            if(!pctype.isTopLevel()) continue;
            if(pctype.fromSource()==null) continue; // probably came from a jar

            String classname=pctype.fullName();
            String filename=pctype.fromSource().path();

            int dotindex=filename.lastIndexOf(".");

            String gotname=filename.substring(0,dotindex).toLowerCase();
            String expectedname=classname.replace('.',File.separatorChar).toLowerCase();

            if(!pctype.flags().isPublic()) {
                // only check the package against the directory
                int gotsepindex=gotname.lastIndexOf(File.separatorChar);
                if(gotsepindex==-1) gotsepindex=0;
                gotname=gotname.substring(0,gotsepindex);

                int expectedsepindex=expectedname.lastIndexOf(File.separatorChar);
                if(expectedsepindex==-1) expectedsepindex=0;
                expectedname=expectedname.substring(0,expectedsepindex);
            }

            if(!gotname.endsWith(expectedname)) {
                job.compiler().errorQueue().enqueue
                    (ErrorInfo.SEMANTIC_ERROR,
                     (pctype.flags().isPublic() ? "public " : "")
                     +"class "+classname+" cannot be defined in file "+filename);
            }
        }
    }
}

/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2005 Pavel Avgustinov
 * Copyright (C) 2007 Eric Bodden
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

package abc.tm.weaving.weaver;

import java.util.Iterator;

import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.weaving.weaver.Weaver;

/** 
 * Modified weaver to implement TraceMatching
 *
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 *  @author Eric Bodden
 */
public class TMWeaver extends Weaver
{
	
    /**
     * set to true before the last (re)weaving pass
     */
    protected boolean nowLastWeavingPass = false;
    
    public void removeDeclareWarnings() {
    	nowLastWeavingPass = true;
    	super.removeDeclareWarnings();
    }

    // TODO: Add a dedicated flag for tracematch codegen debugging
    private void debug(String message)
    { if (abc.main.Debug.v().weaverDriver)
        System.err.println("WEAVER DRIVER (TM) ***** " + message);
    }

    
    public void weaveGenerateAspectMethods() {
        // Generate methods inside aspects needed for code gen and bodies of
        //   methods not filled in by front-end (i.e. aspectOf())
        super.weaveGenerateAspectMethods();
        // also generate the code needed for tracematches, i.e. fill in the
        // advice bodies corresponding to each symbol being matched, and the
        // bodies for the different kinds of 'some' advice.
        debug("Generating code for tracematches");
        TraceMatchCodeGen tmcg = new TraceMatchCodeGen();
        Iterator it = ((TMGlobalAspectInfo)abc.main.Main.v().getAbcExtension().getGlobalAspectInfo()).getTraceMatches().iterator();
        while(it.hasNext()) {
            TraceMatch tm = (TraceMatch)it.next();
            tmcg.fillInTraceMatch(tm);
        }
    }

    public void weaveAdvice()
    {
        super.weaveAdvice();

        Iterator i = ((TMGlobalAspectInfo)
                        abc.main.Main.v().getAbcExtension()
                                         .getGlobalAspectInfo())
                                         .getTraceMatches().iterator();

        while (i.hasNext()) {
            TraceMatch tm = (TraceMatch) i.next();
            CodeGenHelper helper = tm.getCodeGenHelper();

            if (tm.hasITDAnalysisResults()) {
                System.out.println(tm.getITDAnalysisResults());

                if (tm.getITDAnalysisResults().canOptimise())
                    tm.doITDOptimisation();
            }

            helper.extractBodyMethod();
            helper.transformRealBodyMethod();
            helper.genRunSolutions();
        }
    }
}

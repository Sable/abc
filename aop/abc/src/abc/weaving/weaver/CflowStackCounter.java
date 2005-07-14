/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.weaver;
import abc.weaving.matching.*;
import abc.weaving.aspectinfo.*;
import soot.*;
import java.util.*;

/** Counts cflow stacks and counters.
 *  @author Ondrej Lhotak
 */

public class CflowStackCounter {
    private Set setups = new HashSet();

    public void count() {
        for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator mIt = cl.getSootClass().getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                MethodAdviceList mal = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(m);
                if( mal == null ) continue;
                for( Iterator aaIt = mal.allAdvice().iterator(); aaIt.hasNext(); ) {
                    final AdviceApplication aa = (AdviceApplication) aaIt.next();
                    AbstractAdviceDecl ad = aa.advice;
                    if( ad instanceof CflowSetup ) setups.add(ad);
                }
            }
        }
        int stacks = 0;
        int counters = 0;
        for( Iterator cfsIt = setups.iterator(); cfsIt.hasNext(); ) {
            final CflowSetup cfs = (CflowSetup) cfsIt.next();
            if(cfs.getFormals().size() > 0) counters++;
            else stacks++;
        }
        System.err.println( "LaTeX: "+stacks+" & "+counters );
    }
}

/* abc - The AspectBench Compiler
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

package abc.tm.weaving.aspectinfo;

import polyglot.types.SemanticException;

import abc.weaving.aspectinfo.*;
import abc.tm.weaving.weaver.*;

import java.util.*;

/** 
 * Extension of GlobalAspectInfo to store TraceMatch objects. 
 *
 *  @author Julian Tibble
 */
public class TMGlobalAspectInfo extends GlobalAspectInfo
{
    private Map<String,TraceMatch> tmNameToTracematch = new HashMap<String,TraceMatch>();

    public void addTraceMatch(TraceMatch tm)
    {
    	assert !tmNameToTracematch.containsKey(tm.getName());
    	assert !tmNameToTracematch.containsValue(tm);
        tmNameToTracematch.put(tm.getName(), tm);
    }

    public void computeAdviceLists() throws SemanticException
    {
        // transform the tracematch body advice methods
        Iterator i = tmNameToTracematch.values().iterator();

        while (i.hasNext()) {
            TraceMatch tm = (TraceMatch) i.next();
            CodeGenHelper helper = tm.getCodeGenHelper();

            tm.findUnusedFormals();
            helper.transformBodyMethod();
        }

        super.computeAdviceLists();
    }

    public List<TraceMatch> getTraceMatches()
    {
        return new ArrayList<TraceMatch>(tmNameToTracematch.values());
    }
    
    public TraceMatch traceMatchByName(String tmName) {
    	assert tmNameToTracematch.containsKey(tmName);
    	return tmNameToTracematch.get(tmName);
}
    public boolean removeTraceMatch(TraceMatch tm) {
    	return tmNameToTracematch.values().remove(tm);
    }
}

/* abc - The AspectBench Compiler
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collection;

import soot.Context;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.SootField;
import soot.jimple.spark.ondemand.DemandCSPointsTo;

/**
 * A special version of {@link DemandCSPointsTo} that queries the on demand analysis without call graph refinement for certain types. 
 *
 * @author Eric Bodden
 */
public class CustomizedDemandCSPointsTo implements PointsToAnalysis {
    
    protected final DemandCSPointsTo delegate;
    protected final Collection<RefLikeType> typesNoCallgraphRefinement;

    public CustomizedDemandCSPointsTo(DemandCSPointsTo delegate, Collection<RefLikeType> typesNoCallgraphRefinement) {
        this.delegate = delegate;
        this.typesNoCallgraphRefinement = typesNoCallgraphRefinement;
    }

    public PointsToSet reachingObjects(Local l) {
        boolean oldValue = delegate.isRefineCallGraph();
        if(typesNoCallgraphRefinement.contains(l.getType())) {
            delegate.setRefineCallGraph(false);
        } 
        PointsToSet reachingObjects = delegate.reachingObjects(l);
        delegate.setRefineCallGraph(oldValue);
        return reachingObjects;
    }

    public PointsToSet reachingObjects(Context c, Local l, SootField f) {
        return delegate.reachingObjects(c, l, f);
    }

    public PointsToSet reachingObjects(Context c, Local l) {
        return delegate.reachingObjects(c, l);
    }

    public PointsToSet reachingObjects(Local l, SootField f) {
        return delegate.reachingObjects(l, f);
    }

    public PointsToSet reachingObjects(PointsToSet s, SootField f) {
        return delegate.reachingObjects(s, f);
    }

    public PointsToSet reachingObjects(SootField f) {
        return delegate.reachingObjects(f);
    }

    public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
        return delegate.reachingObjectsOfArrayElement(s);
    }
}

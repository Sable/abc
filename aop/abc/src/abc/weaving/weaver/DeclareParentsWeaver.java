/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

import abc.weaving.aspectinfo.*;

import soot.*;

import java.util.*;

/** Weave in the effects of declare parents declarations
 *  @author Aske Simon Christensen
 */
public class DeclareParentsWeaver {

    public void weave() {
        List classesToReResolve = new ArrayList();

	Iterator dpi = GlobalAspectInfo.v().getDeclareParents().iterator();
	while (dpi.hasNext()) {
	    DeclareParents dp = (DeclareParents)dpi.next();
	    List/*<SootClass>*/ classes = new ArrayList();
	    Iterator wci = dp.getClasses().iterator();
	    while (wci.hasNext()) {
		AbcClass wc = (AbcClass)wci.next();
		SootClass sc = wc.getSootClass();
		classes.add(sc);
	    }
	    if (dp instanceof DeclareParentsImpl) {
		List/*<SootClass>*/ ints = new ArrayList();
		Iterator isi = ((DeclareParentsImpl)dp).getInterfaces().iterator();
		while (isi.hasNext()) {
		    AbcClass is = (AbcClass)isi.next();
		    ints.add(is.getSootClass());
		}
		Iterator sci = classes.iterator();
		while (sci.hasNext()) {
		    SootClass sc = (SootClass)sci.next();
		    Iterator sii = ints.iterator();
		    while (sii.hasNext()) {
			SootClass si = (SootClass)sii.next();
			// Make the class implement the interface
			if (abc.main.Debug.v().declareParents) {
			    System.out.println(sc+" implements "+si);
			}
			if (!sc.implementsInterface(si.getName())) {
			    sc.addInterface(si);
                            classesToReResolve.add(sc);
			}
		    }
		}
	    }
	    if (dp instanceof DeclareParentsExt) {
		AbcClass parent = ((DeclareParentsExt)dp).getParent();
		SootClass sp = parent.getSootClass();
		Iterator sci = classes.iterator();
		while (sci.hasNext()) {
		    SootClass sc = (SootClass)sci.next();
		    // Make the class extend the parent
		    if (abc.main.Debug.v().declareParents) {
			System.out.println(sc+" extends "+sp);
		    }
		    sc.setSuperclass(sp);
                    classesToReResolve.add(sc);
		}
	    }
	}

	// Recompute the hierarchy
	Scene.v().releaseActiveHierarchy();
	Scene.v().releaseFastHierarchy();

        // Resolve additional supeclasses
        for( Iterator clsIt = classesToReResolve.iterator(); clsIt.hasNext(); ) {
            final SootClass cls = (SootClass) clsIt.next();
            SootResolver.v().reResolve( cls );
        }
    }
}

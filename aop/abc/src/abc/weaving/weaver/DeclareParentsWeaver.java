
package abc.weaving.weaver;

import abc.weaving.aspectinfo.*;

import soot.*;

import java.util.*;

public class DeclareParentsWeaver {

    public void weave() {
	Iterator dpi = GlobalAspectInfo.v().getDeclareParents().iterator();
	while (dpi.hasNext()) {
	    DeclareParents dp = (DeclareParents)dpi.next();
	    ClassnamePattern pat = dp.getClasses();
	    List/*<SootClass>*/ classes = new ArrayList();
	    Iterator wci = GlobalAspectInfo.v().getWeavableClasses().iterator();
	    while (wci.hasNext()) {
		AbcClass wc = (AbcClass)wci.next();
		SootClass sc = wc.getSootClass();
		if (pat.matchesClass(sc)) {
		    classes.add(sc);
		}
	    }
	    if (dp instanceof DeclareParentsImpl) {
		List/*<SootClass>*/ ints = new ArrayList();
		Iterator isi = ((DeclareParentsImpl)dp).getInterfaces().iterator();
		while (isi.hasNext()) {
		    String is = (String)isi.next();
		    ints.add(Scene.v().getSootClass(is));
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
			sc.addInterface(si);
		    }
		}
	    }
	    if (dp instanceof DeclareParentsExt) {
		String parent = ((DeclareParentsExt)dp).getParent();
		SootClass sp = Scene.v().getSootClass(parent);
		Iterator sci = classes.iterator();
		while (sci.hasNext()) {
		    SootClass sc = (SootClass)sci.next();
		    // Make the class extend the parent
		    if (abc.main.Debug.v().declareParents) {
			System.out.println(sc+" extends "+sp);
		    }
		    sc.setSuperclass(sp);
		}
	    }
	}
    }
}

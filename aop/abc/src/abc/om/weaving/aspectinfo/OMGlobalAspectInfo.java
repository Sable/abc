/*
 * Created on Sep 5, 2005
 *
 */
package abc.om.weaving.aspectinfo;

import abc.om.visit.ModuleStructure;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import polyglot.types.SemanticException;

/**
 * @author Neil Ongkingco
 *
 */
public class OMGlobalAspectInfo extends GlobalAspectInfo {

    public void computeAdviceLists() throws SemanticException {
        ModuleStructure.v().normalizeSigPointcuts();
        super.computeAdviceLists();
    }
}

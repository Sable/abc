package abc.ja.om.modulestruct;

import polyglot.types.SemanticException;
import abc.weaving.aspectinfo.GlobalAspectInfo;

public class OMGlobalAspectInfo extends GlobalAspectInfo {
	public void computeAdviceLists() throws SemanticException {
    	JAModuleStructure moduleStruct = ((abc.ja.om.AbcExtension) abc.main.Main.v().getAbcExtension()).moduleStruct;
    	moduleStruct.normalizeSigPointcuts();
        super.computeAdviceLists();
    }
}

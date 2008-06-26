package abc.ja.om.modulestruct;

import abc.ja.om.jrag.OMOpenClassParent;
import abc.ja.om.jrag.Pattern;

public class JAOpenClassFlagParent extends JAOpenClassFlag {
	
	Pattern allowedParents;
	public JAOpenClassFlagParent(OMOpenClassParent parent) {
		this.allowedParents = parent.getParentAspects();
	}

	@Override
	public boolean isAllowed(JAOpenClassContext context) {
		JAOpenClassContextParent parentContext = (JAOpenClassContextParent) context;
		return allowedParents.matchesType(parentContext.getDeclaredParent()) ;
	}
	
	public String toString() {
		return "parents(" + allowedParents.toString() + ")"; 
	}
	
}

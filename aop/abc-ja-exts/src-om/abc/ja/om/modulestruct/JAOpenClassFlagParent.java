package abc.ja.om.modulestruct;

import abc.ja.om.jrag.OMOpenClassParent;
import abc.ja.om.jrag.Pattern;

public class JAOpenClassFlagParent extends JAOpenClassFlag {
	
	Pattern parents;
	public JAOpenClassFlagParent(OMOpenClassParent parent) {
		//TODO
		this.parents = parent.getParentAspects();
	}

	@Override
	public boolean isAllowed(JAOpenClassContext context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString() {
		return "parents(" + parents.toString() + ")"; 
	}
	
}

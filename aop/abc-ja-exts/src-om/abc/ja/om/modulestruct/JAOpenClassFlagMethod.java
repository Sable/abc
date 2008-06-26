package abc.ja.om.modulestruct;

import abc.ja.om.jrag.OMOpenClassMethod;



public class JAOpenClassFlagMethod extends JAOpenClassFlag {
	public JAOpenClassFlagMethod(OMOpenClassMethod method) {
	}

	@Override
	public boolean isAllowed(JAOpenClassContext context) {
		return true;
	}
	
	public String toString() {
		return "method";
	}
}

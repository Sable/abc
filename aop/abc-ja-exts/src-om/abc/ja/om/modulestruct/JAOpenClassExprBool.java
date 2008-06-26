package abc.ja.om.modulestruct;

import abc.ja.om.modulestruct.JAOpenClassFlagSet.OCFType;

public class JAOpenClassExprBool extends JAOpenClassExpr {

	boolean val = false;
	
	public JAOpenClassExprBool(boolean val) {
		this.val = val;
	}
	
	@Override
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		// TODO Auto-generated method stub
		return val;
	}
	
	public String toString() {
		return Boolean.toString(val);
	}

}

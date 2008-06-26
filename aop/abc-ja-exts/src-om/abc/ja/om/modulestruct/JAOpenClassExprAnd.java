package abc.ja.om.modulestruct;

import abc.ja.om.modulestruct.JAOpenClassFlagSet.OCFType;

public class JAOpenClassExprAnd extends JAOpenClassExprBinary {
	public JAOpenClassExprAnd(JAOpenClassExpr left, JAOpenClassExpr right) {
		super(left, right);
	}

	@Override
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString() {
		return "(" + left.toString() + ")" + " && "  + "(" + right.toString() + ")";
	}
	
}

package abc.ja.om.modulestruct;

import abc.ja.om.modulestruct.JAOpenClassFlagSet.OCFType;

public class JAOpenClassExprOr extends JAOpenClassExprBinary {
	public JAOpenClassExprOr(JAOpenClassExpr left, JAOpenClassExpr right) {
		super(left, right);
	}

	@Override
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		return left.isAllowed(type, context) || right.isAllowed(type, context);
	}
	
	public String toString() {
		return "(" + left.toString() + ")" + " || "  + "(" + right.toString() + ")";
	}
	
}

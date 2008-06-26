package abc.ja.om.modulestruct;

public abstract class JAOpenClassExprBinary extends JAOpenClassExpr {
	JAOpenClassExpr left;
	JAOpenClassExpr right;
	
	public JAOpenClassExprBinary(JAOpenClassExpr left, JAOpenClassExpr right) {
		this.left = left;
		this.right = right;
	}
}

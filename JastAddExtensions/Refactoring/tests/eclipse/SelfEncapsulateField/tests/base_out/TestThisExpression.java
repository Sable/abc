///package base_out;

public class TestThisExpression {
	private int field;
	
	public void foo() {
		this.setField(10);
		new TestThisExpression().setField(11);
	}

	int /// void
	setField(int field) {
		return ///
		this.field = field;
	}

	int getField() {
		return field;
	}
}
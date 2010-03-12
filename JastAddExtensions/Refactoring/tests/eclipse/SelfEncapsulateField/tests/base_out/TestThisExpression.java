///package base_out;

public class TestThisExpression {
	private int field;
	
	public void foo() {
		this.setField(10);
		new TestThisExpression().setField(11);
	}

	int getField() {
		return field;
	}

	int /// void
	setField(int field) {
		return ///
		this.field = field;
	}
}
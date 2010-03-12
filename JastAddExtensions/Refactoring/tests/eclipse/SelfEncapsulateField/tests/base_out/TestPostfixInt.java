///package base_out;

public class TestPostfixInt {
	private int field;
	
	public void foo() {
		setField(getField() + 1);
		setField(getField() - 1);
	}

	int getField() {
		return field;
	}

	int ///void 
        setField(int field) {
		return ///
		this.field = field;
	}
}

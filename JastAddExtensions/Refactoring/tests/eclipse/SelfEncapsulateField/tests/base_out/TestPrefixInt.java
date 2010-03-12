///package base_out;

public class TestPrefixInt {
	private int field;
	
	public void foo() {
		setField(getField() + 1);
		setField(getField() - 1);
		int i;
		i= +getField();
		i= - getField();
		i= ~getField();
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

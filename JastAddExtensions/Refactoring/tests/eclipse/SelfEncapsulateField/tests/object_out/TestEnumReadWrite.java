///package object_out;

public enum TestEnumReadWrite {
	TEST;
	private String field;

	public void foo() {
		setField(getField() + "field");
	}

	public String ///void
	       setField(String field) {
		return ///
		this.field = field;
	}

	public String getField() {
		return field;
	}
}

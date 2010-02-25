///package object_out;

public class TestCStyleArrayRead {
	private Object field[] = new Object[0];

	public TestCStyleArrayRead() {
		setField(new Object[0]);
	}
	public Object[] ///void 
	       setField(Object field[]) {
		return ///
		this.field = field;
	}
	public Object[] getField() {
		return field;
	}
	public void basicRun() {
		System.err.println(getField().length);
	}

}

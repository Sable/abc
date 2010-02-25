///package object_out;

public class TestGenericRead<E> {
	private E field;

	public void foo() {
		E e = getField();
	}

	public E ///void 
	       setField(E field) {
		return ///
		this.field = field;
	}

	public E getField() {
		return field;
	}
}

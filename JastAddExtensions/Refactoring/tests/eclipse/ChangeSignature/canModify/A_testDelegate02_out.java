package p;

///import java.util.List;

class A{
	/**
	 * @deprecated Use {@link #m(List)} instead
	 */
	private void m() {
		m(null);
	}

	private void m(java.util.///
				   List list) {
		m(list);
	}
}

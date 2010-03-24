package p;

class A<E> {
	String x() {
		return (String)bar(); ///return this.<String>bar();
	}

	<T> T bar() {
		return null;
	}
}

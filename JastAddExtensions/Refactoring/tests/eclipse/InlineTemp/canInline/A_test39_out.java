package p;

class A<E> extends Super {
	String x() {
		return (String)super.bar(); ///return super.<String>bar();
	}

	<T> T bar() {
		return null;
	}
}

class Super {
	<T> T bar() {
		return null;
	}
}
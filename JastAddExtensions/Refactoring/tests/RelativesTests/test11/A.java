package p;

class A<T> {
	void m(T t) { }
}

interface I<R> {
	void n(R r);
}


class B extends A<String> implements I<Number> {
	void m(String s) { }
	void m(Integer i) { }
	
	public void n(Number n) { }
	public void n(Integer i) { }
}


class C<P,Q> extends A<P> implements I<Q> {
	void m(P p) { }
	public void n(Q q) { }
}
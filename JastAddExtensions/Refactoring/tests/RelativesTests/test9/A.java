package p;


class A<T> {
	void m(T t) { }
}

class B<R,T> extends A<T> {
	void m(R r) { int is_related = 1; }
}
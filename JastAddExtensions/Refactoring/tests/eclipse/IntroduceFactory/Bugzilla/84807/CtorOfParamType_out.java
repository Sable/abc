package p;

public class CtorOfParamType<T> {
	private CtorOfParamType(T t) { }
	public static <T> CtorOfParamType<T> createCtorOfParamType(T t) {
		return new CtorOfParamType<T>(t);
	}
}

class call {
	void foo() {
		CtorOfParamType<String> x= CtorOfParamType.createCtorOfParamType("");
	}
}
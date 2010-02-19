package p;
class B {
	public void firstMethodToMove(A a, String param) {
		String param0 = param;                  ///
                a.target.secondMethodToMove(a, param0); ///a.target.secondMethodToMove(a, param);
	}

	public void secondMethodToMove(A a, String param) {
		a.methodToStay();
	}
}
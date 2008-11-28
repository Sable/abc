class Test {
    void method(int i) {
        boolean isDirty = /*[*/i == 0/*]*/ || otherTests();
    }

    // added to make it compile
	private boolean otherTests() { return false; }
}
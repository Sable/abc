class Test {
    void method(int i) {
        boolean isDirty = newMethod(i) || otherTests();
    }

    private boolean newMethod(int i) {
        return i == 0;
    }

    // added to make it compile
	private boolean otherTests() { return false; }
}
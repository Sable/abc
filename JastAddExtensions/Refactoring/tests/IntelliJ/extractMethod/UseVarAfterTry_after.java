class A {
    {
	// added initialization to make it compile
        Object o = null;

        try {
            o = newMethod();
        }
        catch (Exception e) {
        }

	// following line changed to make it compile
        o.hashCode();
    }

    private Object newMethod() {
        Object o;
        o = foo();
        return o;
    }

    // method added to make it compile
    Object foo() { return null; }
}
class A {
    {
	// added initialization to make it compile
        Object o = null;

        try {
            /*[*/o = foo();/*]*/
        }
        catch (Exception e) {
        }

	// following line changed to make it compile
        o.hashCode();
    }

    // method added to make it compile
    Object foo() { return null; }
}
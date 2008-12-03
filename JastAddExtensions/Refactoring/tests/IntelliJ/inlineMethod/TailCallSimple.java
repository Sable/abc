class A {
    private String toInline(boolean b) {
        if (b) {
            return "b";
        }
        return "a";
    }

    public String method(boolean b) {
	// added return for compilability
        return /*[*/toInline(b)/*]*/;
    }
}
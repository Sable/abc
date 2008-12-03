class Test {
    void method(Object x) {
        String s = null;
        s = (String) x;
        /*[*/toInline(s.length())/*]*/;
    }
    void toInline(final int i) {
        System.out.println(i);
    }
}

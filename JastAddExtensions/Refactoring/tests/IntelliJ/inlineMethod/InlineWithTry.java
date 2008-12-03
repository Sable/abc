class A {
    {
        /*[*/g()/*]*/;
    }
    int g() {
        try {
            return 0;
        } catch (Error e) {
            throw e;
        }
    }
}

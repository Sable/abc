class A {
    // next line inserted for compilability
    class MyObject { }

    void foo(final MyObject obj) {
        final MyObject _obj;
        _obj = obj;
        new Runnable() {
            public void run() {
                System.out.println(/*[*/_obj/*]*/);
            }
        }.run();
    }
}
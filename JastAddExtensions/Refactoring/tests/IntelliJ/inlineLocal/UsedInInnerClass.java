class A {
    // inserted declaration for compilability
    class MyObject { }

    void foo(final MyObject obj) {
        final MyObject _obj = obj;
        new Runnable() {
            public void run() {
                System.out.println(/*[*/_obj/*]*/);
            }
        }.run();
    }
}
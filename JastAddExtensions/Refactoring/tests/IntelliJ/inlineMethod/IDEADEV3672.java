class Tester1 {
    void caller() {
        /*[*/method()/*]*/; 
    }
    void method() {
        new Runnable() {
            public void run() {
                other();
            }
        };
    }
    void other() { }
}
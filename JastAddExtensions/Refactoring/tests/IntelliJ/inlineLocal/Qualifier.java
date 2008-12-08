class Outer {
    String getValue() { return "";}
    void doSomething (String x) { }
    void foo() {
        new Runnable() {
            public void run() {
                /*[*/final String value = getValue();/*]*/
                doSomething(value);
            }
        };
    }
}

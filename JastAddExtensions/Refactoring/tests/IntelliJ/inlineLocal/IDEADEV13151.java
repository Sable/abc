class Tester {
    void method(String x, String... y) {
    }

    void method1(String x, String[] y) {
    }

    void caller() {
        String[] thing = {"a", "b"};
        method("", /*[*/thing/*]*/);
        method1("", thing);
    }
}
class Test {
    private String s;
    /*[*/String method() {
        s = "Hello";
        return s;
    }/*]*/
    void test() {
        System.out.println(method());
        System.out.println(s);
    }
}
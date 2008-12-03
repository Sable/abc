public enum EEE {
    a(/*[*/doTest()/*]*/);

    EEE(String s) {
    }

    private static String doTest() {
        System.out.println("q");
        return "";
    }
}

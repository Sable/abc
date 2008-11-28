public class Test {
    public static void foo(char c) {
        if (/*[*/c == '\n' || c == '\r'/*]*/ || c == '\u0000') {
            System.out.println("");
        }
    }
}

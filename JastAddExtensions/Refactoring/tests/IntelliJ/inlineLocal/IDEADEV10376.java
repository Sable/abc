class IDEADEV10376 {
    private static int f(int p) {
        int i = 0;
        i = 9;
        i = f(/*[*/i/*]*/);
        return 0;
    }
}
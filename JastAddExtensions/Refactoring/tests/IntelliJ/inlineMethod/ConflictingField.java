class Test {
    public int i;

    public int getI() {
        return i;
    }

    public void usage() {
        int i = /*[*/getI()/*]*/;
    }
}
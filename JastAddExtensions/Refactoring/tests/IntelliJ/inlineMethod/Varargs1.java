class BugTest {
    void f(String... s) {
        for (String s1 : s) {

        }
    }

    {
        /*[*/f(new String[] {""})/*]*/;
    }
}
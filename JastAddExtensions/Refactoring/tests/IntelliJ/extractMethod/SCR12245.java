public class A {
    private void foo() {
        Runnable a = new Runnable() {
            private int a;

            public void run() {
                /*[*/a = 2/*]*/;
            }
        };
    }
}
public class Test {
    public static void main(String[] args) {
        final String s = "dude";
        /*[*/Runnable runnable = new Runnable() {
            public void run() {
                System.out.println(s);
            }
        };
        new Thread(runnable).start();/*]*/
    }
}
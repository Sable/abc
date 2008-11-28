public class Test {
    int method() {
        try {
            System.out.println("Text");
            return 0;
        } finally {
            /*[*/System.out.println("!!!");
            return 1;/*]*/
        }
    }
}
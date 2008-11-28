class Container {
    static class X {
        boolean x = false;

        void foo(String s, String t) {
            /*[*/x = true;/*]*/

            x = true;
        }
    }
}
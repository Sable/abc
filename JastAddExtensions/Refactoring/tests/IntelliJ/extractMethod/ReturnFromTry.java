class A {
    public String method() {
        try {
            /*[*/try {
                return "";
            }
            finally {
                System.out.println("f");
            }/*]*/
        }
        catch (Error e) {

        }
        return "";
    }
}
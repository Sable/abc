class Foo {
    private void bar() {
        String text = null;
        try {
            /*[*/text = getString();/*]*/
        }
        catch(Exception e) {
            System.out.println(text);
        }
    }
    private String getString() {
        return "hello";
    }
}
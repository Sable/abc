/* from the IntelliJ test suite */

class A {
    private void bar() {
        String text = null;
        try {
            // from
	    text = getString();
	    // to
        }
        catch(Exception e) {
            System.out.println(text);
        }
    }
    private String getString() {
        return "hello";
    }
}
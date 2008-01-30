//Listing 5.14 TestException.java: a program that tests exception logging

public class TestException {
    public static void main(String[] args) {
	perform();
    }

    public static void perform() {
	Object nullObj = null;
	nullObj.toString();
    }
}

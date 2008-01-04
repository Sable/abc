package main;

/*
 * used by some of the test harnesses to indicated that a test failed
 */

public class TestingException extends Exception {
    
    public TestingException(String msg) {
        super(msg);
    }

}

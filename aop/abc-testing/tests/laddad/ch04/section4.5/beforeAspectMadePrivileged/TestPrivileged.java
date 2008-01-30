//Listing 4.22 TestPrivileged.java

public class TestPrivileged {
    private static int _lastId = 0;

    private int _id;

    public static void main(String[] args) {
	TestPrivileged test = new TestPrivileged();
	test.method1();
    }

    public TestPrivileged() {
	_id = _lastId++;
    }

    public void method1() {
	System.out.println("TestPrivileged.method1");
    }
}

// ExtractMethod/test19/in/A.java A m 1 1 n

class A {
    int m() {
	int i;
	i = 2;
	return ++i;
    }
    void n() {
	System.out.println("Hello, world!");
    }
}

// ExtractMethod/test30/in/A.java A m 0 0 n

aspect X {
    static int x;
    int A.m(int y) {
	y += x;
	System.out.println(y);
	return y;
    }
}

class A {
}
// ExtractMethod/test20/in/A.java B m 1 1 n

class A {
    int n() {
	return 23;
    }
}

class B extends A {
    int m() {
	int i;
	i = 2;
	return ++i;
    }
}

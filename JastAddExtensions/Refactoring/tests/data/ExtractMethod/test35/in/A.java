// ExtractMethod/test35/in/A.java A test 1 1 n

class A {
    int test(int y) {
	int x;
	if(y > 0) {
	    x = 1;
	    y = y + x;
	}
	x = y;
	y = y + x;
	return y;
    }
}

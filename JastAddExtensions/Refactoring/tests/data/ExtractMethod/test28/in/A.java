// ExtractMethod/test28/in/A.java A m 3 4 n

class A {
    void m() {
	int y;
	int z;
	y=2;
	try {
	    if(3==3)
		y = 1;
	    else
		throw new Exception("boo");
	} catch(Throwable t) {
	}
	z=y;
    }
}
// ExtractMethod/test27/in/A.java A m 2 3 n

class A {
    void m() {
	int y;
	int z;
	try {
	    if(3==3)
		y = 1;
	    else
		throw new Exception("boo");
	} catch(Throwable t) {
	    y=2;
	}
	z=y;
    }
}
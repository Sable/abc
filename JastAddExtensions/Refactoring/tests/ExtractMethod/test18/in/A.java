// ExtractMethod/test18/in/A.java A m 1 2 n
import java.io.FileNotFoundException;

class A {
    void m() throws FileNotFoundException {
	int i;
	i = 2;
	if(i==2)
	    throw new FileNotFoundException("");
	int j = ++i;
    }
}
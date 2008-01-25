// ExtractMethod/test22/in/A.java A m 1 3 n
import java.io.FileNotFoundException;

class A {
    int m() throws FileNotFoundException {
	int i;
	i = 2;
	if(i==2)
	    throw new FileNotFoundException("");
	int j = ++i;
	return j;
    }
}
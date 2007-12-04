// ExtractMethod/test10/in/A.java A m 3 3 n
import java.io.FileNotFoundException;

class A {
	void m() throws FileNotFoundException {
		int i;
		i = 2;
		for(int j=0;j<i;++j) {
			if(j==4)
				throw new FileNotFoundException("");
			++i;
		}
		int j = ++i;
	}
}
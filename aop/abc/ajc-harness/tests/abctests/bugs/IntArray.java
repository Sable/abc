import org.aspectj.testing.Tester;

public class IntArray {
    public static void main(String[] args) throws Exception {
	int[] a, b;
	b = new int[1];
	b[0] = 73;
	a = (int[])b.clone();
	Tester.checkEqual(b[0], 42);
    }
}

aspect A {
    after(int[] a) : call(* int[].clone()) && target(a) {
	a[0] = 42;
    }
}

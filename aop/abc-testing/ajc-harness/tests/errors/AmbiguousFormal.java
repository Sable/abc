public class AmbiguousFormal {
    public void m(int x, int y) {}
}

aspect A {
    before(int n): execution(void *.m(..)) && args(.., n, ..) {
	System.out.println(n);
    }
}

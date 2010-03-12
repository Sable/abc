// 5, 23 -> 5, 38
class Foo {
	public final static String BASE = "base."; //$NON-NLS-1$
	public void m1() {
		String name = CONSTANT;
	}
	private static final String CONSTANT= BASE + "suffix";
}
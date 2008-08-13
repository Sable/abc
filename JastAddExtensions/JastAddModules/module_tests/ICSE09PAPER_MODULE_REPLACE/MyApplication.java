module myapplication;
public class MyApplication {
	StaticAnalyzer s = new StaticAnalyzer();
	JavadocGenerator g = new JavadocGenerator();

	public MyApplication() {
		System.out.println(this.getClass());
	}
}


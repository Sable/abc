module staticanalyzer;
import parser.Parser;
public class StaticAnalyzer {
	Parser p = new Parser();
	public StaticAnalyzer() {
		System.out.println(this.getClass());
	}
}

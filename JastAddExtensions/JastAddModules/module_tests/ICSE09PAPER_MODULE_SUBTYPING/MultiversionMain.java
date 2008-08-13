module multiversion;
public class MultiversionMain {
	parserV1_1::parser.Parser p1 = new parserV1_1::parser.Parser();
	parserV1_2::parser.Parser p2 = new parserV1_2::parser.Parser();
	public MultiversionMain() {
		System.out.println(this.getClass());
	}
}

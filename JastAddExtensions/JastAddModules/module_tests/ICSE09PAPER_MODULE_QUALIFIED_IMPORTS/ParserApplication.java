module org.x.y.parserapplication;
package org.x.y.parserapplication;

import parser::org.x.y.parser.Parser;
import parser::scanner::org.x.y.scanner.*;

public class ParserApplication {
	Parser p = new Parser();
	Scanner s = new Scanner();
	public ParserApplication() {
		System.out.println(this.getClass());
	}
}

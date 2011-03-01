package tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import AST.RTXF;

public class ExtractBlockTests extends TestCase {
	public static TestSuite suite() {
		return RTXF.makeSuite("tests/ExtractBlock.xml");
	}
}
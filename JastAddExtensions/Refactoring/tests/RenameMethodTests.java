package tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import AST.RTXF;

public class RenameMethodTests extends TestCase {
	public static TestSuite suite() {
		return RTXF.makeSuite("tests/RenameMethod.xml");
	}
}
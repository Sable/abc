package tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import AST.RTXF;

public class RenamePackageTests extends TestCase {
	public static TestSuite suite() {
		return RTXF.makeSuite("tests/RenamePackage.xml");
	}
}
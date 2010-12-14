package tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import AST.Program;
import AST.RTXF;

public class RenamePackageTests extends TestCase {
	public static TestSuite suite() {
		return RTXF.makeSuite("tests/RenamePackage.xml");
	}
	
	private void outputTestCase(String old_name, String new_name, Program in, Program out) {
		System.out.println("<testcase>");
		System.out.println("  <refactoring>");
		System.out.println("    <rename newname=\"" + RTXF.sanitise(new_name) + "\">");
		System.out.println("      <pkgref name=\"" + RTXF.sanitise(old_name) + "\"/>");
		System.out.println("    </rename>");
		System.out.println("  </refactoring>");
		System.out.print(RTXF.program(2, in));
		if(out == null)
			System.out.print(RTXF.result(2, true));
		else
			System.out.print(RTXF.result(2, false, out));
		System.out.println("</testcase>");
	}
}
package tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import AST.RTXF;

public class ExtractClassTests extends TestCase {
	public static TestSuite suite() {
		return RTXF.makeSuite("tests/ExtractClass.xml");
	}
	
/*	private int i = 1;
	private void outputTestCase(String newClassName, String newFieldName, String[] fns, Program in, Program out, boolean encapsulate, boolean toplevel) {
		StringBuffer fields = new StringBuffer();
		for(int i=0;i<fns.length;++i) {
			if(i>0)
				fields.append(" ");
			fields.append(fns[i]);
		}
		System.out.println("<testcase name=\"test" + (i++) + "\">");
		System.out.println("  <refactoring>");
		System.out.println("    <extract_class classname=\"" + RTXF.sanitise(newClassName) + "\"" +
											 " fieldname=\"" + RTXF.sanitise(newFieldName) + "\"" +
											 " fields=\"" + RTXF.sanitise(fields.toString()) + "\"" +
											 " encapsulate=\"" + (encapsulate ? "yes" : "no") + "\"" +
											 " toplevel=\"" + (toplevel ? "yes" : "no") + "\"" + "/>");
		System.out.println("  </refactoring>");
		System.out.print(RTXF.program(2, in));
		if(out == null)
			System.out.print(RTXF.result(2, true));
		else
			System.out.print(RTXF.result(2, false, out));
		System.out.println("</testcase>");
	}*/
}
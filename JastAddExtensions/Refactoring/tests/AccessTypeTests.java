package tests;

import AST.Dot;
import AST.FileRange;
import AST.TypeAccess;

public class AccessTypeTests extends AccessType {

	public AccessTypeTests(String arg0) {
		super(arg0);
	}

	public void testTypeAccess() {
		runTypeAccessTest(new FileRange("Access/test15/Test.java", 17, 1, 19, 1), new FileRange("Access/test15/Test.java", 13, 16, 13, 16), new TypeAccess("A"));
		runTypeAccessTest(new FileRange("Access/test16/Test.java", 18, 5, 18, 19), new FileRange("Access/test16/Test.java", 12, 19, 12, 19), new TypeAccess("B"));
		runTypeAccessTest(new FileRange("Access/test17/Test.java", 14, 5, 16, 5), new FileRange("Access/test17/Test.java", 12, 16, 12, 20), new TypeAccess("A"));
		runTypeAccessTest(new FileRange("Access/test17/Test.java", 15, 9, 15, 19), new FileRange("Access/test17/Test.java", 12, 19, 12, 19), new TypeAccess("B"));
		runTypeAccessTest(new FileRange("Access/test19/Test.java", 11, 1, 16, 1), new FileRange("Access/test19/Test.java", 14, 17, 14, 19), new TypeAccess("Access.test19", "Test"));
		runTypeAccessTest(new FileRange("Access/test20/Test.java", 12, 6, 12, 40), new FileRange("Access/test20/Test.java", 15, 16, 15, 16), new Dot(new TypeAccess("Test"), new TypeAccess("Foo")));
		runTypeAccessTest(new FileRange("Access/test21/Test.java", 10, 1, 15, 1), new FileRange("Access/test21/Test.java", 13, 16, 13, 16), new TypeAccess("Test"));
		runTypeAccessTest(new FileRange("Access/test21/pkg1/Test.java", 3, 1, 5, 1), new FileRange("Access/test21/Test.java", 13, 16, 13, 16), new TypeAccess("Access.test21.pkg1", "Test"));
		runTypeAccessTest(new FileRange("Access/test23/Test.java", 10, 1, 11, 1), new FileRange("Access/test23/Test.java", 14, 5, 14, 7), new TypeAccess("Test"));
		runTypeAccessTest(new FileRange("Access/test39/A.java", 5, 5, 7, 5), new FileRange("Access/test39/A.java", 16, 7, 16, 9), new TypeAccess("XYZ"));
		runTypeAccessTest(new FileRange("Access/test40/Test.java", 12, 5, 17, 5), new FileRange("Access/test40/Test.java", 15, 27, 15, 39), new TypeAccess("Inner1"));
	}
}
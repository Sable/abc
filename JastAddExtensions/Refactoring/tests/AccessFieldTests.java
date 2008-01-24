package tests;

import AST.CastExpr;
import AST.Dot;
import AST.FileRange;
import AST.ParExpr;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.VarAccess;

public class AccessFieldTests extends AccessField {

	public AccessFieldTests(String arg0) {
		super(arg0);
	}

	public void testFieldAccess() {
		runFieldAccessTest(new FileRange("Access/test24/Test.java", 12, 5, 12, 11), new FileRange("Access/test24/Test.java", 13, 14, 13, 15), new VarAccess("a"));
		runFieldAccessTest(new FileRange("Access/test25/Test.java", 12, 5, 12, 11), new FileRange("Access/test25/Test.java", 13, 5, 13, 16), new VarAccess("a"));
		runFieldAccessTest(new FileRange("Access/test26/Test.java", 11, 5, 11, 14), new FileRange("Access/test26/Test.java", 16, 7, 16, 15), new Dot(new SuperAccess("super"), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test27/Test.java", 12, 5, 12, 14), new FileRange("Access/test27/Test.java", 19, 24, 19, 32), new Dot(new SuperAccess("super"), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test28/Test.java", 12, 5, 12, 14), new FileRange("Access/test28/Test.java", 20, 24, 20, 32), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test29/Test.java", 11, 6, 11, 13), new FileRange("Access/test29/Test.java", 16, 14, 16, 25), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test30/Test.java", 12, 6, 12, 13), new FileRange("Access/test30/Test.java", 19, 25, 19, 36), new Dot(new ParExpr(new CastExpr(new TypeAccess("B"), new ThisAccess("this"))), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test31/Test.java", 12, 5, 12, 14), new FileRange("Access/test31/Test.java", 20, 24, 20, 32), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
		runFieldAccessTest(new FileRange("Access/test32/Test.java", 11, 5, 11, 19), new FileRange("Access/test32/Test.java", 13, 16, 13, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
		runFieldAccessTest(new FileRange("Access/test33/Test.java", 11, 5, 11, 19), new FileRange("Access/test33/Test.java", 14, 16, 14, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
		runFieldAccessTest(new FileRange("Access/test34/Test.java", 11, 5, 11, 19), new FileRange("Access/test34/Test.java", 18, 16, 18, 23), new Dot(new SuperAccess("super"), new VarAccess("bar")));
		runFieldAccessTest(new FileRange("Access/test35/Test.java", 11, 5, 11, 19), new FileRange("Access/test35/Test.java", 14, 24, 14, 36), new VarAccess("bar"));
		runFieldAccessTest(new FileRange("Access/test36/Test.java", 11, 5, 11, 19), new FileRange("Access/test36/Test.java", 15, 27, 15, 39), new Dot(new TypeAccess("Inner1"), new Dot(new ThisAccess("this"), new VarAccess("bar"))));
		runFieldAccessTest(new FileRange("Access/test37/Test.java", 11, 5, 11, 12), new FileRange("Access/test37/Test.java", 17, 24, 17, 26), new VarAccess("bar"));
		runFieldAccessTest(new FileRange("Access/test38/Test.java", 12, 5, 12, 12), new FileRange("Access/test38/Test.java", 19, 24, 19, 41), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new Dot(new TypeAccess("Test"), new ThisAccess("this")))), new VarAccess("bar")));
	}
}
package tests;

import AST.CastExpr;
import AST.Dot;
import AST.FileRange;
import AST.ParExpr;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.VarAccess;

public class EncapsulateFieldTests extends EncapsulateField {

	public EncapsulateFieldTests(String arg0) {
		super(arg0);
	}

	public void testEncapsulateField() {
		runEncapsulationTest("test1");
		runEncapsulationTest("test2");
		runEncapsulationTest("test3");
		runEncapsulationTest("test4");
		runEncapsulationTest("test5");
		runEncapsulationTest("test6");
		runEncapsulationTest("test7");
		runEncapsulationTest("test8");
		runEncapsulationTest("test9");
		runEncapsulationTest("test10");
		runEncapsulationTest("test11");
		runEncapsulationTest("test12");
		runEncapsulationTest("test13");
		runEncapsulationTest("test14");
		runEncapsulationTest("test15");
		runEncapsulationTest("test16");
		runEncapsulationTest("test17");
		runEncapsulationTest("test18");
		runEncapsulationTest("test19");
		runEncapsulationTest("test20");
		runEncapsulationTest("test21");
	}
}
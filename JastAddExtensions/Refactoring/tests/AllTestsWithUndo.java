package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsWithUndo {

	public static Test suite() {
		AllTests.TEST_UNDO = true;
		TestSuite suite = new TestSuite("Refactoring Tests with Undo");
		//$JUnit-BEGIN$
		suite.addTestSuite(ExtractBlockTests.class);
		suite.addTestSuite(ExtractClassTests.class);
		suite.addTestSuite(ExtractConstantTests.class);
		suite.addTestSuite(ExtractMethodTests.class);
		suite.addTestSuite(ExtractTempTests.class);
		suite.addTestSuite(InlineConstantTests.class);
		suite.addTestSuite(InlineMethodTests.class);
		suite.addTestSuite(InlineTempTests.class);
		suite.addTestSuite(LocalClassToMemberClassTests.class);
		suite.addTestSuite(MakeMethodStaticTests.class);
		suite.addTestSuite(MoveMemberTypeToToplevelTests.class);
		suite.addTestSuite(MoveMethodTests.class);
		suite.addTestSuite(PromoteTempToFieldTests.class);
		suite.addTestSuite(PullUpMethodTests.class);
		suite.addTestSuite(PushDownMethodTests.class);
		suite.addTestSuite(RemoveUnusedMethodTests.class);
		suite.addTestSuite(RenameMethodTests.class);
		suite.addTestSuite(RenamePackageTests.class);
		suite.addTestSuite(RenameTypeTests.class);
		suite.addTestSuite(RenameVariableTests.class);
//		suite.addTestSuite(RelativesTests.class);
		suite.addTestSuite(MakeMethodAbstract.class);
		suite.addTestSuite(tests.eclipse.ExtractTemp.ExtractTempTests.class);
		suite.addTestSuite(tests.eclipse.MoveInstanceMethod.MoveInstanceMethodTests.class);
		suite.addTestSuite(tests.eclipse.PushDown.PushDownTests.class);
		suite.addTestSuite(tests.eclipse.PullUp.PullUpTests.class);
		suite.addTestSuite(tests.eclipse.RenamePackage.RenamePackageTests.class);
		suite.addTestSuite(tests.eclipse.MoveInnerToTopLevel.MoveInnerToTopLevelTests.class);
		suite.addTestSuite(tests.eclipse.ConvertAnonymousToNested.ConvertAnonymousToNestedTests.class);
		suite.addTestSuite(tests.eclipse.SelfEncapsulateField.SelfEncapsulateFieldTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceParameterObject.IntroduceParameterObjectTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceIndirection.IntroduceIndirectionTests.class);
		suite.addTestSuite(tests.eclipse.ExtractClass.ExtractClassTests.class);
		suite.addTestSuite(tests.eclipse.MoveMembers.MoveMembersTests.class);
		suite.addTestSuite(tests.eclipse.ExtractConstant.ExtractConstantTests.class);
		suite.addTestSuite(tests.eclipse.InlineConstant.InlineConstantTests.class);
		//suite.addTestSuite(tests.eclipse.ChangeSignature.ChangeSignatureTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceParameter.IntroduceParameterTests.class);
		suite.addTestSuite(tests.eclipse.PromoteTempToField.PromoteTempToFieldTests.class);
		suite.addTestSuite(tests.eclipse.InlineTemp.InlineTempTests.class);
//		suite.addTestSuite(tests.eclipse.IntroduceFactory.IntroduceFactoryTests.class);
		suite.addTestSuite(tests.eclipse.RenameMethodInInterface.RenameMethodInInterfaceTests.class);
		suite.addTestSuite(tests.eclipse.RenameNonPrivateField.RenameNonPrivateFieldTests.class);
		suite.addTestSuite(tests.eclipse.RenameParameters.RenameParametersTests.class);
		suite.addTestSuite(tests.eclipse.RenamePrivateField.RenamePrivateFieldTests.class);
		suite.addTestSuite(tests.eclipse.RenamePrivateMethod.RenamePrivateMethodTests.class);
		suite.addTestSuite(tests.eclipse.RenameStaticMethod.RenameStaticMethodTests.class);
		suite.addTestSuite(tests.eclipse.RenameTemp.RenameTempTests.class);
		suite.addTestSuite(tests.eclipse.RenameType.RenameTypeTests.class);
		suite.addTestSuite(tests.eclipse.RenameTypeParameter.RenameTypeParameterTests.class);
		suite.addTestSuite(tests.eclipse.RenameVirtualMethodInClass.RenameVirtualMethodInClassTests.class);
		//$JUnit-END$
		return suite;
	}

}
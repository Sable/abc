package jester;

public class RealClassTestTester implements ClassTestTester {
	private MutationsList myMutationsList;
	private TestRunner myTestRunner;

	public RealClassTestTester(TestRunner testRunner, MutationsList aMutationsList) {
		super();
		myTestRunner = testRunner;
		myMutationsList = aMutationsList;
	}

	private void test(CodeMangler codeMangler, ClassSourceCodeChanger sourceCodeSystem) throws SourceChangeException {
		boolean madeChange = codeMangler.makeChangeToClass();
		while (madeChange) {
			boolean testsDidNotCatchChange = myTestRunner.testsRunWithoutFailures();
			if (testsDidNotCatchChange) {
				sourceCodeSystem.lastChangeDidNotCauseTestsToFail();
			} else {
				sourceCodeSystem.lastChangeCausedTestsToFail();
			}
			madeChange = codeMangler.makeChangeToClass();
		}
	}
	
	public void testUsing(final ClassSourceCodeChanger sourceCodeSystem) throws SourceChangeException {
		sourceCodeSystem.startJesting();

		CodeMangler aSimpleIntCodeMangler = new SimpleIntCodeMangler(sourceCodeSystem);
		test(aSimpleIntCodeMangler, sourceCodeSystem);

		MutationMaker aMutationMaker = new MutationMaker() {
			public void mutate(String changeFrom1, String changeTo1) throws SourceChangeException {
				CodeMangler aSimpleCodeMangler = new TwoStringSwappingCodeMangler(sourceCodeSystem, changeFrom1, changeTo1);
				test(aSimpleCodeMangler, sourceCodeSystem);
			}
		};

		myMutationsList.visit(aMutationMaker);

		sourceCodeSystem.finishJesting();
	}
}
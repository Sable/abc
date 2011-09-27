
public aspect Check{

	private pointcut IgnoreCalls(): call(* java..*.*(..));
	
	private pointcut IgnoreTargets(): get(static * java..*.*);
	
	public static void main(String[] args){
		System.out.println("hello");
	}
	
	before() : IgnoreTargets(){
		//do something
	}
	
}

/***
 * the traceback that I obtain when I execute this program.  I think that abc doesn't support
 * this kind of pattern. 
 */
//Running test 1662: abctests/jpi/Check
//Commandline: abc -d abctests/jpi -warn-unused-advice:off -ext abc.ja.jpi abctests/jpi/LawOfDemeter/Check.aj 
//Unexpected exception while compiling: polyglot.util.InternalCompilerError: unhandled exception during weaving/optimisation
//polyglot.util.InternalCompilerError: unhandled exception during weaving/optimisation
//	at abc.main.CompileSequence.runSequence(CompileSequence.java:142)
//	at abc.main.Main.run(Main.java:406)
//	at abc.testing.TestCase.runTest(TestCase.java:289)
//	at abc.testing.Main.doCase(Main.java:288)
//	at abc.testing.Main.main(Main.java:122)
//Caused by: polyglot.util.InternalCompilerError: abctests/jpi/LawOfDemeter/Check.aj:10: matchesType should only be called on the root name pattern
//	at abc.weaving.matching.AdviceApplication.doShadows(AdviceApplication.java:202)
//	at abc.main.AbcExtension.findMethodShadows(AbcExtension.java:557)
//	at abc.weaving.matching.AdviceApplication.doMethod(AdviceApplication.java:275)
//	at abc.weaving.matching.AdviceApplication.computeAdviceLists(AdviceApplication.java:312)
//	at abc.weaving.aspectinfo.GlobalAspectInfo.computeAdviceLists(GlobalAspectInfo.java:482)
//	at abc.ja.jpi.JPIGlobalAspectInfo.computeAdviceLists(JPIGlobalAspectInfo.java:20)
//	at abc.ja.jpi.CompileSequence.weave(CompileSequence.java:153)
//	at abc.main.CompileSequence.runSequence(CompileSequence.java:114)
//	... 4 more
//Caused by: polyglot.util.InternalCompilerError: matchesType should only be called on the root name pattern
//	at abc.ja.jpi.jrag.DotDotNamePattern.matchesType_compute(DotDotNamePattern.java:249)
//	at abc.ja.jpi.jrag.DotDotNamePattern.matchesType(DotDotNamePattern.java:241)
//	at abc.ja.jpi.jrag.DotNamePattern.matchesTypeAndName_compute(DotNamePattern.java:313)
//	at abc.ja.jpi.jrag.DotNamePattern.matchesTypeAndName(DotNamePattern.java:300)
//	at abc.ja.jpi.jrag.FieldPattern.matchesFieldRef(FieldPattern.java:108)
//	at abc.weaving.aspectinfo.GetField.matchesAt(GetField.java:55)
//	at abc.weaving.aspectinfo.ShadowPointcut.matchesAt(ShadowPointcut.java:38)
//	at abc.weaving.matching.AdviceApplication$2.run(AdviceApplication.java:115)
//	at abc.weaving.matching.AdviceApplication.doShadows(AdviceApplication.java:192)
//	... 11 more
//FAIL: Test 1662: "abctests/jpi/Check" failed in 8093ms, memory usage: 46312136.

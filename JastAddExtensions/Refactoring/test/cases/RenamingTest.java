package test.cases;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import changes.RefactoringException;

public class RenamingTest extends TestCase {
	
	public RenamingTest(String method) {
		super(method);
	}
	
	public void testRenameSuccessful(String classname, String fieldname, String newname, 
			String srcfile, SuccessChecker sc) {
		try {
			String[] srcfiles = { srcfile };
			System.out.println("rename("+classname+", "+fieldname+", "+newname+", {"+srcfile+"})");
			List res = main.RenameTest.rename(classname, fieldname, newname, srcfiles);
			assert sc.check(res);
		} catch(RefactoringException e) {
			assert false;
		}
	}
	
	public void testRenameFailing(String classname, String fieldname, String newname,
			String srcfile, FailureChecker fc) {
		try {
			String[] srcfiles = { srcfile };
			main.RenameTest.rename(classname, fieldname, newname, srcfiles);
			Assert.fail();
		} catch(RefactoringException e) {
			Assert.assertTrue(fc.check(e.getMessage()));
		}
	}

}

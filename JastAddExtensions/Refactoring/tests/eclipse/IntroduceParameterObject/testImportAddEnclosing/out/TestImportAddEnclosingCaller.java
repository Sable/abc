package p;

import java.security.Permission;

///import p.TestImportAddEnclosing.FooParameter;

public class TestImportAddEnclosingCaller {
	public void bar(){
		new TestImportAddEnclosing().foo(
		    new TestImportAddEnclosing.///
			FooParameter(new Permission[0], 7));
	}
}

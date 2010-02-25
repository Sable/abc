package p;

import java.security.Permission;

public class TestImportAddEnclosingCaller {
	public void bar(){
		new TestImportAddEnclosing().foo(new Permission[0], 7);
	}
}

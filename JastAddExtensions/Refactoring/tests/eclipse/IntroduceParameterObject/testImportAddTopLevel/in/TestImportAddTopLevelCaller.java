package p.sub;

import java.security.Permission;

public class TestImportAddTopLevelCaller {
	public void bar(){
		new p.TestImportAddTopLevel().foo(new Permission[0], 99);
	}
}

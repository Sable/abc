package p.sub;

import java.security.Permission;

///import p.parameters.TestImportAddTopLevelParameter;

public class TestImportAddTopLevelCaller {
	public void bar(){
		new p.TestImportAddTopLevel().foo(new 
		    p.///
		    TestImportAddTopLevelParameter(new Permission[0], 99));
	}
}

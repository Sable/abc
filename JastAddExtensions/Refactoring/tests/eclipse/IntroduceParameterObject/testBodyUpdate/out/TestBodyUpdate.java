package p;

import java.security.Permission;

class s {
	int i(){return 5;}
}

public class TestBodyUpdate extends s{
	Permission p;
	public static class FooParameter {
		public Permission p;
		public String s;
		public int i;
		public FooParameter(Permission p, String s, int i) {
			this.p = p;
			this.s = s;
			this.i = i;
		}
	}
	public void foo(FooParameter parameterObject){
		Permission p = parameterObject.p; ///
		String s = parameterObject.s;
		int i = parameterObject.i;
		i=i();
		s=new s().i()+"";
		i+=super.i();
		this.p=p; ///parameterObject.p;
	}
	
}

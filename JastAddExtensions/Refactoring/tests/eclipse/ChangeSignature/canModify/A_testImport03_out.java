package p;

///import java.security.acl.Permission;

abstract class A {
	public abstract int m(java.security.acl.///
					      Permission p, java.security.Permission pp);
	protected void finalize() {
		m(null, null);
	}
}

class B extends A {
	public int m(java.security.acl.///
			     Permission p, java.security.Permission pp) {
		return 17;
	}
}

//Listing 4.23 PrivilegeTestAspect.java

public aspect PrivilegeTestAspect {
    before(TestPrivileged callee) : call(void TestPrivileged.method1())
	&& target(callee) {
	System.out.println("<PrivilegeTestAspect:before objectId=\""
			   + callee._id + "\"");
    }
}

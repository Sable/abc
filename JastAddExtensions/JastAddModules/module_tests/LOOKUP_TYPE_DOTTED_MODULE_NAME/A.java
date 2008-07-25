module com.xyz.m1;
import java.util.List;
import java.util.LinkedList;

public class A {
	//should resolve to java.util.List due to precedence of cu import
	List<A> list = new LinkedList<A>();
	//needs qualifier since module imports come after cu imports
	jastadd$framework.List flist = new jastadd$framework.List();
	Type1 t1 = new Type1();
	B b = new B();
	Type2 t2 = new Type2();
	com.xyz.m2::com.xyz.m3::C c = new com.xyz.m2::com.xyz.m3::C();
	com.xyz.m2::com.xyz.m3::Type3 t3 = new com.xyz.m2::com.xyz.m3::Type3();
	a.AA aa = new a.AA();
	com.xyz.m2::b.BB bb = new com.xyz.m2::b.BB();
}

module m1;
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
	m2::m3::C c = new m2::m3::C();
	m2::m3::Type3 t3 = new m2::m3::Type3();
}

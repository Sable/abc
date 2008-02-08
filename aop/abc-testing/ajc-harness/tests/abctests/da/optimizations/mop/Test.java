import org.aspectj.testing.Tester;
import java.util.*;

public class Test {

	public static void main(String[] args) {
		Collection c = new HashSet();
		c.add("1");
		c.add("2");
		Iterator i = c.iterator();
		i.next();		//must stay
		i.next();		//must stay
		Iterator i2 = c.iterator();
		i2.next();		//must stay
		i2.hasNext();	//must stay
		i2.next();		//must stay
		Iterator i3 = c.iterator();
		i3.hasNext();	//can be removed
		i3.hasNext();	//can be removed
		
		Tester.check(mop.HasNext.c_hasNext==1,"c_hasNext should equal 1, is: "+mop.HasNext.c_hasNext);
		Tester.check(mop.HasNext.c_next==4,"c_next should equal 4, is: "+mop.HasNext.c_next);
	}

}
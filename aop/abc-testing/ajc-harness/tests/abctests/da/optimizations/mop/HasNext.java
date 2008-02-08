/*+ Monitor aspect for +*/
package mop;
import java.util.*;
class CiHasNextMonitor {
	int state = 0;
	boolean failed = false;boolean suceeded = false;public CiHasNextMonitor() {
		boolean hasnext = false;
		boolean next = false;
	}
	public void hasnext(Iterator i) {
		boolean hasnext = false;
		boolean next = false;
		hasnext = true;
		switch (state) {
			case 0 :
			state = next ? 1 : -1;
			break;
			case 1 :
			state = next ? 0 : -1;
			break;
			default:
			state = -1;
		}
		failed = state == -1;
		suceeded = state == 0;
	}
	public void next(Iterator i) {
		boolean hasnext = false;
		boolean next = false;
		next = true;
		switch (state) {
			case 0 :
			state = next ? 1 : -1;
			break;
			case 1 :
			state = next ? 0 : -1;
			break;
			default:
			state = -1;
		}
		failed = state == -1;
		suceeded = state == 0;
	}
	public boolean failed(){
		return failed;
	}
	public boolean suceeded(){
		return suceeded;
	}
	public void reset(){
		boolean hasnext = false;
		boolean next = false;
		state = 0;
	}
}
public aspect HasNext {

	public static int c_hasNext = 0;
	public static int c_next = 0;
	
	dependency {
		strong next;
		weak hasNext;
	}

	Map makeMap(Object key){
		if (key instanceof String)
		return new HashMap();
		else
		return new IdentityHashMap();
	}
	List makeList(){
		return new ArrayList();
	}
	Map CiHasNext_i_Map = null;
	
	pointcut CiHasNext_hasnext0(Iterator i) :  call(* Iterator.hasNext())&& target(i)&& !within(dacapo..*) && !within(tests..*)&& !within(HasNext)&& !within(CiHasNextMonitor);

	dependent after hasNext(Iterator i) : CiHasNext_hasnext0(i) {
		c_hasNext++;
		Object obj = null;
		CiHasNextMonitor monitor = null;
		Map m = CiHasNext_i_Map;
		if (m == null) m = CiHasNext_i_Map = makeMap(i);
		{
			obj = m.get(i);
		}//end of lookup
		monitor = (CiHasNextMonitor)obj;
		if (monitor != null) {
			monitor.hasnext(i);
			if (monitor.failed()) {
				monitor.reset();
			}//end if
			if (monitor.suceeded()) {
				System.out.println("hasNext() has not been called before calling next() for an iterator");
			}//end if
		}//end if
	}

	pointcut CiHasNext_next0(Iterator i) :  call(* Iterator.next())&& target(i)&& !within(dacapo..*) && !within(tests..*)&& !within(HasNext)&& !within(CiHasNextMonitor);

	dependent before next(Iterator i): CiHasNext_next0(i) {
		c_next++;
		Object obj = null;
		CiHasNextMonitor monitor = null;
		boolean toCreate = false;
		Map m = CiHasNext_i_Map;
		if (m == null) m = CiHasNext_i_Map = makeMap(i);
		{
			obj = m.get(i);
			monitor = (CiHasNextMonitor)obj;
			toCreate = (monitor == null);
			if (toCreate){
				monitor = new CiHasNextMonitor();
				m.put(i, monitor);
			}//end of if
		}//end of lookup
		{
			monitor.next(i);
			if (monitor.failed()) {
				monitor.reset();
			}
			if (monitor.suceeded()) {
				System.out.println("hasNext() has not been called before calling next() for an iterator");
			}
		}
	}
}
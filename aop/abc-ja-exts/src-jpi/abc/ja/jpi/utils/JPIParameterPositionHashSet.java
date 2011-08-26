package abc.ja.jpi.utils;

import java.util.HashSet;
import java.util.Iterator;

public class JPIParameterPositionHashSet extends HashSet<JPIParameterPosition>{

	private static final long serialVersionUID = 1L;
	
	/***
	 * by default we look at for the position index
	 */
	@Override
	public boolean contains(Object o) {
		JPIParameterPosition pos;
		for (Iterator<JPIParameterPosition> i=iterator(); i.hasNext();) {
			pos = i.next();
			if (pos.getPosition().equals(o)){
				return true;
			}
		}
		return false;
	}

	/***
	 * we look at for the parent position index
	 */
	public boolean containsParent(Object o) {
		JPIParameterPosition pos;
		for (Iterator<JPIParameterPosition> i=iterator(); i.hasNext();) {
			pos = i.next();
			if (pos.getParentPosition().equals(o)){
				return true;
			}
		}
		return false;
	}	
	
	public JPIParameterPosition getByParentPosition(Object o){
		JPIParameterPosition pos = null;
		for (Iterator<JPIParameterPosition> i=iterator(); i.hasNext();) {
			pos = i.next();
			if (pos.getParentPosition().equals(o)){
				pos.setAccessed();
				return pos;
			}
		}
		return pos;
	}
	
	public JPIParameterPosition getByPosition(Object o){
		JPIParameterPosition pos = null;
		for (Iterator<JPIParameterPosition> i=iterator(); i.hasNext();) {
			pos = i.next();
			if (pos.getPosition().equals(o)){
				pos.setAccessed();
				return pos;
			}
		}
		return pos;
	}	
}

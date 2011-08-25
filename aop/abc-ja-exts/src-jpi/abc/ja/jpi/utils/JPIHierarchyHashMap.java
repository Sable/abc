package abc.ja.jpi.utils;

import java.util.HashMap;

import abc.ja.jpi.jrag.JPITypeDecl;

public class JPIHierarchyHashMap extends HashMap<JPITypeDecl, Object>{

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean containsKey(Object key) {
		JPIHierarchyHashMap local;
		if (super.containsKey(key)){
			return true;
		}
		for(Object value : values()){
			if(value == null){ continue; }
			local = (JPIHierarchyHashMap)value;
			if (local.containsKey(key)){
				return true;
			}
		}
		return false;
	}
}

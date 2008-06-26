package abc.ja.om.modulestruct;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class JAOpenClassFlagSet {
	
	HashMap<OCFType, JAOpenClassFlag> flags;
	
	public JAOpenClassFlagSet() {
		this.flags = new HashMap<OCFType, JAOpenClassFlag>();
	}
	
	public void addFlag(JAOpenClassFlag flag) {
		if (flag instanceof JAOpenClassFlagField) {
			flags.put(OCFType.FIELD, flag);
		} else if (flag instanceof JAOpenClassFlagMethod) {
			flags.put(OCFType.METHOD, flag);
		} else if (flag instanceof JAOpenClassFlagParent) {
			flags.put(OCFType.PARENT, flag);
		}
	}
	
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		JAOpenClassFlag flag = flags.get(type);
		if (flag == null) {
			return false;
		}
		return flag.isAllowed(context);
	}
	
	public static enum OCFType {
		FIELD,
		METHOD,
		PARENT
    };
	
	public String toString() {
		String ret = "";
		boolean first = true;
		for (JAOpenClassFlag flag : flags.values()) {
			if (!first) {
				ret += ", ";
			}
			ret += flag.toString();
			first = false;
		}
		return ret;
	}
	
}

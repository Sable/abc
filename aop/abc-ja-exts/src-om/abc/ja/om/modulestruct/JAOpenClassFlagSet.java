package abc.ja.om.modulestruct;

import java.util.Collection;
import java.util.HashSet;

public class JAOpenClassFlagSet {
	
	Collection<JAOpenClassFlag> flags;
	
	public JAOpenClassFlagSet(Collection<JAOpenClassFlag> flags) {
		this.flags = flags;
	}
	
	public JAOpenClassFlagSet() {
		this.flags = new HashSet<JAOpenClassFlag>();
	}
	
	public void addFlag(JAOpenClassFlag flag) {
		flags.add(flag);
	}
	
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		//TODO
		return false;
	}
	
	public static class OCFType {
        public OCFType(){};
    }
	
	public String toString() {
		String ret = "";
		boolean first = true;
		for (JAOpenClassFlag flag : flags) {
			if (!first) {
				ret += ", ";
			}
			ret += flag.toString();
			first = false;
		}
		return ret;
	}
}

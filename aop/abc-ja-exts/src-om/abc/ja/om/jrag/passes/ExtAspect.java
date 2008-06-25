package abc.ja.om.jrag.passes;

import java.util.Set;
import java.util.HashSet;

public class ExtAspect implements abc.om.visit.ModulePrecedence {

	String name;

	public ExtAspect(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public String toString() {
		return name;
	}

	public Set getAspectNames() {
		Set ret = new HashSet();
		ret.add(name);
		return ret;
	}
}

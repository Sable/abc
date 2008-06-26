package abc.ja.om;

import abc.ja.om.jrag.OMIncludeMemberType;
import abc.ja.om.jrag.OMModuleDecl;

//a container for the module's parent and include type
public class OMParentModule {
	OMIncludeMemberType type;
	OMModuleDecl parent;
	
	public OMParentModule(OMIncludeMemberType type, OMModuleDecl parent) {
		this.type = type;
		this.parent = parent;
	}

	public OMIncludeMemberType getType() {
		return type;
	}

	public OMModuleDecl getParent() {
		return parent;
	}
	
	public String toString() {
		return parent.getModuleName() + ", " + type.getID();
	}
}

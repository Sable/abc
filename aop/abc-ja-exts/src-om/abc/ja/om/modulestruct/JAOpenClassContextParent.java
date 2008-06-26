package abc.ja.om.modulestruct;

import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.TypeDecl;

public class JAOpenClassContextParent extends JAOpenClassContext {
	TypeDecl classParent;
	public JAOpenClassContextParent(TypeDecl classDecl, AspectDecl aspectDecl, TypeDecl classParent) {
		super(classDecl, aspectDecl);
		this.classParent = classParent;
	}
	public TypeDecl getClassParent() {
		return classParent;
	}
}

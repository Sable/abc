package abc.ja.om.modulestruct;

import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.TypeDecl;

public class JAOpenClassContextParent extends JAOpenClassContext {
	TypeDecl declaredParent;
	public JAOpenClassContextParent(TypeDecl classDecl, AspectDecl aspectDecl, TypeDecl declaredParent) {
		super(classDecl, aspectDecl);
		this.declaredParent = declaredParent;
	}
	public TypeDecl getDeclaredParent() {
		return declaredParent;
	}
}

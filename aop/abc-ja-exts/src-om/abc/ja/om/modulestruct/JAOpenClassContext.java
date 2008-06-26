package abc.ja.om.modulestruct;

import abc.ja.om.jrag.AspectDecl;
import abc.ja.om.jrag.TypeDecl;

//not enough code reuseable in MSOpenClassContext to be worth subtyping
//refactor into interfaces and implementing classes if future need
//occurs
//classDecl = class on which the ITD was inserted
//aspectDecl = aspect where the ITD was declared
public abstract class JAOpenClassContext {

	protected TypeDecl classDecl;
	protected AspectDecl aspectDecl;

	public JAOpenClassContext(TypeDecl classDecl, AspectDecl aspectDecl) {
		this.classDecl = classDecl;
		this.aspectDecl = aspectDecl;
	}
	
	public TypeDecl getClassDecl() {
		return classDecl;
	}
	
	public AspectDecl getAspectDecl() {
		return aspectDecl;
	}
}

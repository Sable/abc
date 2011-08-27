package abc.ja.jpi.utils;

import abc.ja.jpi.jrag.Access;

public class CJPAdviceDeclElement {
	
	private final String kind;
	private final Access jpiAccess;
	
	public CJPAdviceDeclElement(String kind, Access jpiAccess) {
		super();
		this.kind = kind;
		this.jpiAccess = jpiAccess;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj instanceof CJPAdviceDeclElement){
			CJPAdviceDeclElement temp = (CJPAdviceDeclElement)obj;
			if (this.kind.equals(temp.getKind()) && this.jpiAccess.type().equals(temp.getAccess().type())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.kind);
		buffer.append(this.jpiAccess);
		return buffer.toString().hashCode();
	}

	public String getKind() {
		return this.kind;
	}
	
	public Access getAccess(){
		return this.jpiAccess;
	}

}

package abc.ja.om.modulestruct;

import abc.ja.om.jrag.Pattern;
import abc.ja.om.modulestruct.JAOpenClassFlagSet.OCFType;

public class JAOpenClassExprBase extends JAOpenClassExpr {

	protected Pattern cpe; //the pattern after the colon (the classes affected by the permissions)
	protected Pattern toCpe; //to clause cpe
	protected JAOpenClassFlagSet flags;
	
	public JAOpenClassExprBase(JAOpenClassFlagSet flags, 
								Pattern cpe,
								Pattern toCpe) {
		//TODO
		this.flags = flags;
		this.cpe = cpe;
		this.toCpe = toCpe;
	}
	
	@Override
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString() {
		return "openclass " + flags.toString() + 
			" to " + toCpe.toString() + 
			" : " + cpe.toString();
	}
}

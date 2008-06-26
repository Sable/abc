package abc.ja.om.modulestruct;
import abc.ja.om.jrag.*;

public class JAOpenClassFlagField extends JAOpenClassFlag {
	public JAOpenClassFlagField(OMOpenClassField field) {
	}

	@Override
	public boolean isAllowed(JAOpenClassContext context) {
		return true;
	}
	
	public String toString() {
		return "field";
	}
}

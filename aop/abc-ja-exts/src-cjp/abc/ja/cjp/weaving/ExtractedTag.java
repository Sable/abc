package abc.ja.cjp.weaving;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class ExtractedTag implements Tag {
	
	private final String cjpTypeName;

	public ExtractedTag(String cjpTypeName) {
		this.cjpTypeName = cjpTypeName;		
	}
	
	public String getCjpTypeName() {
		return cjpTypeName;
	}

	@Override
	public String getName() {
		return "abc.ja.cjp.weaving.ExtractedTag";
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		throw new AttributeValueException();
	}

}

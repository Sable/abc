//selection: 12, 10, 12, 46
//name: i -> length
package simple;

import java.util.Vector;

public class ConstantExpression2 {
	private Vector fBeginners;
	private Vector fAdvanced;
	
	private int count() {
		return fBeginners.size() + fAdvanced.size();
	}
	public void use() {
		count();
	}
}

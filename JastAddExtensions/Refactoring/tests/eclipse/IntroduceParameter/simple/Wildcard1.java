//selection: 13, 13, 13, 25
//name: number -> number
package simple;

import java.util.List;

public class Wildcard1 {
	private List<? extends Number> field= null;
	private void use() {
		foo();
	}
	private void foo() {
		Number n= field.get(0);
	}
}

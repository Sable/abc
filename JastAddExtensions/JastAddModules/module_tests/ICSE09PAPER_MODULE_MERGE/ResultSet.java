module sqltypes;
package types;

public class ResultSet {
	public ResultSet() {
		System.out.println(this.getClass());
	}
	protected String source = "";
	public ResultSet(String source) {
		this.source = source;
		System.out.println(this.getClass());
	}
	public String getSource() {
		return this.source;
	}
}

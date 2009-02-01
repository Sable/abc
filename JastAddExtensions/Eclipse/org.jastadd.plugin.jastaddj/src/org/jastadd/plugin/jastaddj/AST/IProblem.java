package org.jastadd.plugin.jastaddj.AST;

public interface IProblem {
	public int line();
	public int endLine();
	public int column();
	public int endColumn();
	public String message();
	public Severity severity();
	public Kind kind();
	public static class Severity {
		public static final Severity ERROR = new Severity();
		public static final Severity WARNING = new Severity();
		private Severity() { }
	}
	public static class Kind {
		public static final Kind OTHER = new Kind();
		public static final Kind LEXICAL = new Kind();
		public static final Kind SYNTACTIC = new Kind();
		public static final Kind SEMANTIC = new Kind();
		private Kind() { }
	}
	
	public void setStartOffset(int startOffset);
	public void setEndOffset(int endOffset);
}

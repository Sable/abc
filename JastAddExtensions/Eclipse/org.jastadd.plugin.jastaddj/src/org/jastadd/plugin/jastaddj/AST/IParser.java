package org.jastadd.plugin.jastaddj.AST;

public interface IParser {
	public ICompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception;
	public Object newInternalParser();
}

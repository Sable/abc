package org.jastadd.plugin.jastaddj.compiler.recovery;

import java.io.*;

import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.SOF;

public class Main {
	public static final int VERSION_I = 0;
	public static final int VERSION_II = 1;
	public static final int VERSION_III = 2;

	public static void fullRecovery(String inFile, String outFile, int version) throws IOException {
		StringBuffer buf = readFile(new File(inFile));
		JavaLexer lexer = null;
		
		switch (version) {
			case VERSION_I: lexer = new JavaLexer(); break;
			case VERSION_II: lexer = new JavaLexerII(); break;
			case VERSION_III: lexer = new JavaLexerIII(); break;
		}
		if (lexer != null) {
			SOF sof = lexer.parse(buf);
			Recovery.buildBridges(sof);
			Recovery.recover(sof);
			buf = Recovery.prettyPrint(sof);	
			writeToFile(buf, outFile);
		}
	}

	private static StringBuffer readFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(file));				
		char[] chars = new char[1024];
		int read = 0;
		while((read = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars, 0, read));
		}
		reader.close();
		return sb;
	}


	private static void writeToFile(StringBuffer buf, String file) throws IOException {
        	FileWriter fstream = new FileWriter(file);
    		BufferedWriter out = new BufferedWriter(fstream);
    		out.write(buf.toString());
    		out.close();
	}
}

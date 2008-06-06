package abc.ja.om.parse;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction;

public interface OMAbcLexer extends AbcLexer {
	int module_state();
	void addModuleKeyword(String keyword, LexerAction ka);
}

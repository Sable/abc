package org.jastadd.plugin.jastaddj.search;

import org.jastadd.plugin.search.AbstractBaseSearchResultPage;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.util.FileUtil;

public class JastAddJSearchResultPage extends AbstractBaseSearchResultPage {

	public static final String SEARCH_ID = "org.jastadd.plugin.jastaddj.search.ResultPage";
	
	@Override
	protected void openEditorForNode(IJastAddNode node) {
		FileUtil.openFile(node);
	}

}

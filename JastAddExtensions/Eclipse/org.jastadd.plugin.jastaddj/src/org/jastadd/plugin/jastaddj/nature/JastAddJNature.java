package org.jastadd.plugin.jastaddj.nature;

import org.jastadd.plugin.jastaddj.builder.JastAddJBuilder;
import org.jastadd.plugin.resources.JastAddNature;

public class JastAddJNature extends JastAddNature {

	public static final String NATURE_ID = "org.jastadd.plugin.jastaddj.JastAddJNature";

	@Override
	protected String getBuilderID() {
		return JastAddJBuilder.BUILDER_ID;
	}

}

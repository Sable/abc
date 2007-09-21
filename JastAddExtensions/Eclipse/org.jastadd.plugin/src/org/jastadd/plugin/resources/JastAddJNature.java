package org.jastadd.plugin.resources;

import org.jastadd.plugin.builder.JastAddJBuilder;

public class JastAddJNature extends JastAddNature {

	public static final String NATURE_ID = "org.jastadd.plugin.JastAddJNature";

	@Override
	protected String getBuilderID() {
		return JastAddJBuilder.BUILDER_ID;
	}

}

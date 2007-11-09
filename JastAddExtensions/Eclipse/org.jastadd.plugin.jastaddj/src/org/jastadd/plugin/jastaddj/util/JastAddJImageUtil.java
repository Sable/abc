package org.jastadd.plugin.jastaddj.util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.util.JastAddBaseImageUtil;

public class JastAddJImageUtil extends JastAddBaseImageUtil {
	private final static IPath ICONS_PATH = new Path("icons/");
	
	private static final String T_OBJ= "obj16";
	
	public static final String IMG_OBJS_CUNIT = "IMG_OBJS_CUNIT";
	
	static {
		registerImage(IMG_OBJS_CUNIT, T_OBJ, "java.gif");
	}
	
	public static Image getImage(String key) {
		return JastAddBaseImageUtil.getImage(key);
	}
	
	private static void registerImage(String key, String prefix, String name) {
		IPath path= ICONS_PATH.append(prefix).append(name);
		registerImage(key, JastAddJActivator.getInstance().getBundle(), path);
	}
}

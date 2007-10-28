package org.jastadd.plugin.jastaddj.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JastAddJBuildConfiguration implements Serializable {
	
	public static class Pattern implements Serializable {
		public String value;
		
		public Pattern copy() {
			Pattern newInstance = new Pattern();
			newInstance.value = value;
			return newInstance;
		}
		
		public void update(Pattern otherInstance) {
			value = otherInstance.value;			
		}
	}
	
	public static class SourcePathEntry implements Serializable  {
		public String sourcePath = "";
		public List<Pattern> includeList = new ArrayList<Pattern>();
		public List<Pattern> excludeList = new ArrayList<Pattern>();
		
		public void update(SourcePathEntry otherInstance) {
			sourcePath = otherInstance.sourcePath;
			includeList = new ArrayList<Pattern>(otherInstance.includeList);
			excludeList = new ArrayList<Pattern>(otherInstance.excludeList);
		}
		
		public SourcePathEntry copy() {
			SourcePathEntry newInstance = new SourcePathEntry();
			newInstance.sourcePath = sourcePath;
			
			newInstance.includeList = new ArrayList<Pattern>();
			for(Pattern pattern : includeList)
				newInstance.includeList.add(pattern.copy());

			newInstance.excludeList = new ArrayList<Pattern>();
			for(Pattern pattern : excludeList)
				newInstance.excludeList.add(pattern.copy());

			return newInstance;
		}
	}
	
	public static class ClassPathEntry implements Serializable {
		public String classPath = "";
		public String sourceAttachmentPath;
		
		public void update(ClassPathEntry otherInstance) {
			classPath = otherInstance.classPath;
			sourceAttachmentPath = otherInstance.sourceAttachmentPath;
		}
		
		public ClassPathEntry copy() {
			ClassPathEntry newInstance = new ClassPathEntry();
			//newInstance.isRelative = isRelative;
			newInstance.classPath = classPath;
			newInstance.sourceAttachmentPath = sourceAttachmentPath;
			return newInstance;
		}
	}
	
	public List<SourcePathEntry> sourcePathList = new ArrayList<SourcePathEntry>();
	public List<ClassPathEntry> classPathList = new ArrayList<ClassPathEntry>();
	
	public String outputPath;	
	
	public JastAddJBuildConfiguration copy() {
		JastAddJBuildConfiguration newInstance = new JastAddJBuildConfiguration();
		newInstance.sourcePathList = new ArrayList<SourcePathEntry>();
		for(SourcePathEntry sourceEntry : sourcePathList)
			newInstance.sourcePathList.add(sourceEntry.copy());
		newInstance.classPathList = new ArrayList<ClassPathEntry>();
		for(ClassPathEntry classPathEntry : classPathList)
			newInstance.classPathList.add(classPathEntry.copy());
		newInstance.outputPath = outputPath;		
		return newInstance;
	}
}

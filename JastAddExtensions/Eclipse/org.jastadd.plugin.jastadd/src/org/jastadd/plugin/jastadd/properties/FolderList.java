package org.jastadd.plugin.jastadd.properties;

import java.util.ArrayList;
import java.util.List;

public class FolderList {

	public static class Pattern {
		public String value;
		
		public Pattern(String value) {
			this.value = value;
		}
		
		public Pattern copy() {
			Pattern newInstance = new Pattern(value);
			return newInstance;
		}
		
		public void update(Pattern otherInstance) {
			value = otherInstance.value;			
		}
	}
	
	public static interface PathEntry {
		public String getPath();

		public void setPath(String path);
	}
	
	public static class FileEntry implements PathEntry  {
		private String path = "";
		
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}
	
	public static class FolderEntry implements PathEntry {
		private String path = "";
		private List<Pattern> includeList = new ArrayList<Pattern>();
		private List<Pattern> excludeList = new ArrayList<Pattern>();
		private List<Pattern> fileList = new ArrayList<Pattern>();
		
		public void update(FolderEntry otherInstance) {
			path = otherInstance.path;
			includeList = new ArrayList<Pattern>(otherInstance.includeList);
			excludeList = new ArrayList<Pattern>(otherInstance.excludeList);
			fileList = new ArrayList<Pattern>(otherInstance.fileList);
		}
		
		public FolderEntry copy() {
			FolderEntry newInstance = new FolderEntry();
			newInstance.path = path;
			
			newInstance.includeList = new ArrayList<Pattern>();
			for(Pattern pattern : includeList)
				newInstance.includeList.add(pattern.copy());

			newInstance.excludeList = new ArrayList<Pattern>();
			for(Pattern pattern : excludeList)
				newInstance.excludeList.add(pattern.copy());

			newInstance.fileList = new ArrayList<Pattern>();
			for(Pattern pattern : fileList)
				newInstance.fileList.add(pattern.copy());
			
			return newInstance;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public List<Pattern> getIncludeList() {
			return includeList;
		}

		public void setIncludeList(List<Pattern> includeList) {
			this.includeList = includeList;
		}

		public List<Pattern> getExcludeList() {
			return excludeList;
		}

		public void setExcludeList(List<Pattern> excludeList) {
			this.excludeList = excludeList;
		}

		public List<Pattern> getFileList() {
			return fileList;
		}

		public void setFileList(List<Pattern> fileList) {
			this.fileList = fileList;
		}
		
		public void fileUp(Pattern file) {
			if (fileList.contains(file) && fileList.indexOf(file) > 0) {
				int entry = fileList.indexOf(file);
				fileList.set(entry, fileList.get(entry-1));
				fileList.set(entry-1, file);
			}
		}
		
		public void fileDown(Pattern file) {
			if (fileList.contains(file) && fileList.indexOf(file) < (fileList.size()-1)) {
				int entry = fileList.indexOf(file);
				fileList.set(entry, fileList.get(entry+1));
				fileList.set(entry+1, file);
			}
		}
	}
	
	private List<PathEntry> folderList = new ArrayList<PathEntry>();
	private String resource;
	private String filter;
	private String outputFolder;
	
	public FolderList(String resource, String filter) {
		this.resource = resource;
		this.filter = filter;
	}
	
	public String getResource() {
		return resource;
	}
	
	public void add(PathEntry folderEntry) {
		folderList.add(folderEntry);
	}
	
	public void remove(PathEntry folderEntry) {
		folderList.remove(folderEntry);
	}
	
	public List<PathEntry> entries() {
		return new ArrayList<PathEntry>(folderList);
	}
	
	public void up(PathEntry folderEntry) {
		if (folderList.contains(folderEntry) && folderList.indexOf(folderEntry) > 0) {
			int entry = folderList.indexOf(folderEntry);
			folderList.set(entry, folderList.get(entry-1));
			folderList.set(entry-1, folderEntry);
		}
	}
	
	public void down(PathEntry folderEntry) {
		if (folderList.contains(folderEntry) && folderList.indexOf(folderEntry) < (folderList.size()-1)) {
			int entry = folderList.indexOf(folderEntry);
			folderList.set(entry, folderList.get(entry+1));
			folderList.set(entry+1, folderEntry);
		}
	}

	public String getFilter() {
		return filter;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public static class ParserFolderList extends FolderList {

		private String parserName;
		
		public ParserFolderList(String resource, String filter) {
			super(resource, filter);
		}
		
		public String getParserName() {
			return parserName;
		}

		public void setParserName(String parserName) {
			this.parserName = parserName;
		}
	}
}

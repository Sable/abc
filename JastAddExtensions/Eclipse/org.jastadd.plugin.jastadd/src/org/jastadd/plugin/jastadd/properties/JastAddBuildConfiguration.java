package org.jastadd.plugin.jastadd.properties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jastadd.plugin.jastadd.properties.FolderList.FileEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.FolderEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.ParserFolderList;
import org.jastadd.plugin.jastadd.properties.FolderList.PathEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.Pattern;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Stores the list of flex/parser files for this project.
 * Also deals with persistence by writing out/back from XML file fragments.  
 * @author luke
 *
 */
public class JastAddBuildConfiguration {


	public final static String FLEX_RESOURCE = "flex.xml";
	public final static String PARSER_RESOURCE = "parser.xml";
	public final static String FLEX_FILTER = "*.flex";
	public final static String PARSER_FILTER = "*.parser";
	
	public final static String JASTADD_RESOURCE = "jastadd.xml";

	private static final String OUTPUT_FOLDER_TAG = "output";
	private static final String PARSER_TAG = "parser";
	private static final String FILESET_ENTRY_TAG = "fileset";
	private static final String EXCLUDING_TAG = "exclude";
	private static final String INCLUDING_TAG = "include";
	private static final String FILE_TAG = "file";
	private static final String FOLDER_TAG = "folder";
	
	private static final String JASTADD_TAG = "jastadd";
	private static final String PACKAGE_TAG = "package";

	private static final String DIR_ATTR = "dir";
	private static final String NAME_ATTR = "name";
	private static final String FILE_ATTR = "file";


	public FolderList flex = new FolderList(FLEX_RESOURCE, FLEX_FILTER);
	public ParserFolderList parser = new ParserFolderList(PARSER_RESOURCE, PARSER_FILTER);

	public PackageEntry jastadd = new PackageEntry(JASTADD_RESOURCE);
	
	public static class PackageEntry {
		private String pack;
		private String resource;
		public PackageEntry(String resource) {
			this.resource = resource;
		}
		public void setPackage(String p) {
			pack = p;
		}
		public String getPackage() {
			return pack; 
		}
		public String getResource() {
			return resource;
		}
	}

	public JastAddBuildConfiguration(IProject project) {
		flex = readBuildConfiguration(project, FLEX_RESOURCE, FLEX_FILTER, false);
		FolderList folder = readBuildConfiguration(project, PARSER_RESOURCE, PARSER_FILTER, true);
		if (folder instanceof ParserFolderList)
			parser = (ParserFolderList)folder;
		else parser = new ParserFolderList(PARSER_RESOURCE, PARSER_FILTER);
		jastadd = readBuildConfiguration(project, JASTADD_RESOURCE);
	}
	
	public static PackageEntry readBuildConfiguration(IProject project, String resource) {
		IFile rscFile = project.getFile(resource);
		try {
			if (rscFile.exists()) {
				InputStream stream = rscFile.getContents(true);

				Element cpElement;
				try {
					DocumentBuilder dparser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					cpElement = dparser.parse(new InputSource(stream)).getDocumentElement();
				} finally {
					stream.close();
				}
				
				PackageEntry packageEntry = new PackageEntry(resource);

				// Get the output folder directory
				{
					NodeList list = cpElement.getElementsByTagName(PACKAGE_TAG);
					if (list.getLength() > 0) {
						Node node = list.item(0);
						NamedNodeMap attributes = node.getAttributes();
						String pack = readAttribute(attributes, NAME_ATTR);
						if (pack != null) {
							packageEntry.setPackage(pack);
						}
					}
				}

				return packageEntry;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return new PackageEntry(resource);
	}

	public static FolderList readBuildConfiguration(IProject project, String resource, String filter, boolean parser) {
		IFile rscFile = project.getFile(resource);
		try {
			if (rscFile.exists()) {
				InputStream stream = rscFile.getContents(true);

				Element cpElement;
				try {
					DocumentBuilder dparser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					cpElement = dparser.parse(new InputSource(stream)).getDocumentElement();
				} finally {
					stream.close();
				}
				
				FolderList folderList;
				if (parser) {
					 folderList = new ParserFolderList(resource, filter);
				} else {
					folderList = new FolderList(resource, filter);
				}

				// Get the output folder directory
				{
					NodeList list = cpElement.getElementsByTagName(OUTPUT_FOLDER_TAG);
					if (list.getLength() > 0) {
						Node node = list.item(0);
						NamedNodeMap attributes = node.getAttributes();
						String dir = readAttribute(attributes, DIR_ATTR);
						if (dir != null) {
							folderList.setOutputFolder(dir);
						}
					}
				}

				// Get the parser name.
				if (parser) {
					NodeList list = cpElement.getElementsByTagName(PARSER_TAG);
					if (list.getLength() > 0) {
						Node node = list.item(0);
						NamedNodeMap attributes = node.getAttributes();
						String name = readAttribute(attributes, NAME_ATTR);
						if (name != null) {
							((ParserFolderList) folderList).setParserName(name);
						}
					}
				}

				NodeList list = cpElement.getElementsByTagName(FILESET_ENTRY_TAG);
				for (int i = 0; i < list.getLength(); ++i) {
					Node node = list.item(i);
					NamedNodeMap attributes = node.getAttributes();
					String dir = readAttribute(attributes, DIR_ATTR);
					String file = readAttribute(attributes, FILE_ATTR);
					if (dir == null && file == null) {
						continue;
					} else if (file !=null) {
						FileEntry fileEntry = new FileEntry();
						fileEntry.setPath(file);
						folderList.add(fileEntry);
					} else if (dir != null) {

						FolderEntry folderEntry = new FolderEntry();
						folderEntry.setPath(dir);

						if (node instanceof Element) {
							Element element = (Element) node;
							NodeList childNodes = element.getElementsByTagName(INCLUDING_TAG);
							List<Pattern> includeList = new LinkedList<Pattern>();

							for (int j = 0; j < childNodes.getLength(); ++j) {
								Node childNode = childNodes.item(j);
								String name = readAttribute(childNode.getAttributes(), NAME_ATTR);
								includeList.add(new Pattern(name));
							}
							folderEntry.setIncludeList(includeList);

							childNodes = element.getElementsByTagName(EXCLUDING_TAG);
							List<Pattern> excludeList = new LinkedList<Pattern>();

							for (int j = 0; j < childNodes.getLength(); ++j) {
								Node childNode = childNodes.item(j);
								String name = readAttribute(childNode.getAttributes(), NAME_ATTR);
								excludeList.add(new Pattern(name));
							}
							folderEntry.setExcludeList(excludeList);

							childNodes = element.getElementsByTagName(FILE_TAG);
							List<Pattern> fileList = new LinkedList<Pattern>();

							for (int j = 0; j < childNodes.getLength(); ++j) {
								Node childNode = childNodes.item(j);
								String name = readAttribute(childNode.getAttributes(), NAME_ATTR);
								fileList.add(new Pattern(name));
							}
							folderEntry.setFileList(fileList);
						}
						folderList.add(folderEntry);
					}
				}
				return folderList;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return new FolderList(resource, filter);
	}	

	protected static String readAttribute(NamedNodeMap attributes, String attribute) {
		Attr attr = (Attr) attributes.getNamedItem(attribute);
		if (attr != null)
			return attr.getValue();
		else
			return null;
	}
	
	public static void writePackageEntry(IProject project, PackageEntry packageEntry) throws CoreException {
		// Compose XML
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		XMLWriter xmlWriter;
		try {
			xmlWriter = new XMLWriter(byteOutputStream);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		xmlWriter.startTag(JASTADD_TAG, new HashMap<String, String>(), true);

		if (packageEntry.getPackage() != null) {
			HashMap<String, String> packageMap = new HashMap<String, String>();
			packageMap.put(NAME_ATTR, packageEntry.getPackage());
			xmlWriter.startTag(PACKAGE_TAG, packageMap, true);
			xmlWriter.endTag(PACKAGE_TAG);
		}

		xmlWriter.endTag(JASTADD_TAG);

		xmlWriter.flush();
		xmlWriter.close();

		// Write bytes
		InputStream byteInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());

		IFile rscFile = project.getFile(packageEntry.getResource());
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only file
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[] { rscFile }, null);
			}
			rscFile.setContents(byteInputStream, IResource.FORCE, null);
		} else {
			rscFile.create(byteInputStream, IResource.FORCE, null);
		}		
	}
	

	public static void writeFolderList(IProject project, FolderList folderList) throws CoreException {
		// Compose XML
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		XMLWriter xmlWriter;
		try {
			xmlWriter = new XMLWriter(byteOutputStream);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		xmlWriter.startTag(FOLDER_TAG, new HashMap<String, String>(), true);

		if (folderList.getOutputFolder() != null) {
			HashMap<String, String> outputFolderMap = new HashMap<String, String>();
			outputFolderMap.put(DIR_ATTR, folderList.getOutputFolder());
			xmlWriter.startTag(OUTPUT_FOLDER_TAG, outputFolderMap, true);
			xmlWriter.endTag(OUTPUT_FOLDER_TAG);
		}

		
		if (folderList instanceof ParserFolderList) {
			ParserFolderList parserList = (ParserFolderList) folderList;
			if (parserList.getParserName() != null) {
				HashMap<String, String> parserNameMap = new HashMap<String, String>();
				parserNameMap.put(NAME_ATTR, parserList.getParserName());
				xmlWriter.startTag(PARSER_TAG, parserNameMap, true);
				xmlWriter.endTag(PARSER_TAG);
			}
		}

		// Write folder entries
		for (PathEntry pathEntry : folderList.entries()) {
			if (pathEntry instanceof FolderEntry) {
				// writing a proper file set
				FolderEntry folderEntry = (FolderEntry) pathEntry;
				HashMap<String, String> folderEntryMap = new HashMap<String, String>();
				folderEntryMap.put(DIR_ATTR, folderEntry.getPath());
				xmlWriter.startTag(FILESET_ENTRY_TAG, folderEntryMap, true);
				outputPatternList(folderEntry.getIncludeList(), INCLUDING_TAG, xmlWriter);
				outputPatternList(folderEntry.getExcludeList(), EXCLUDING_TAG, xmlWriter);
				outputPatternList(folderEntry.getFileList(), FILE_TAG, xmlWriter);
				xmlWriter.endTag(FILESET_ENTRY_TAG);
			} else {
				// write the filename
				HashMap<String, String> pathEntryMap = new HashMap<String, String>();
				pathEntryMap.put(FILE_ATTR, pathEntry.getPath());
				xmlWriter.startTag(FILESET_ENTRY_TAG, pathEntryMap, true);
				xmlWriter.endTag(FILESET_ENTRY_TAG);
			}
		}

		xmlWriter.endTag(FOLDER_TAG);

		xmlWriter.flush();
		xmlWriter.close();

		// Write bytes
		InputStream byteInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());

		IFile rscFile = project.getFile(folderList.getResource());
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only file
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[] { rscFile }, null);
			}
			rscFile.setContents(byteInputStream, IResource.FORCE, null);
		} else {
			rscFile.create(byteInputStream, IResource.FORCE, null);
		}
	}

	private static void outputPatternList(List<Pattern> list, String tag, XMLWriter xmlWriter) {
		for(Pattern pattern : list) {
			HashMap<String,String> attributes = new HashMap<String, String>();
			attributes.put(NAME_ATTR, pattern.value);
			xmlWriter.startTag(tag, attributes, true);
			xmlWriter.endTag(tag);
		}

	}
}

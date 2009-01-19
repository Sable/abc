package org.jastadd.plugin.jastaddj.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jastadd.plugin.jastaddj.Activator;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.ClassPathEntry;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JastAddJBuildConfigurationUtil {
	public final static String RESOURCE = ".classpath";

	private static final String CLASSPATH_TAG = "classpath";
	private static final String CLASSPATH_ENTRY_TAG = "classpathentry";
	
	private static final String KIND_ATTR = "kind";
	private static final String EXCLUDING_ATTR = "excluding";
	private static final String INCLUDING_ATTR = "including";
	private static final String PATH_ATTR = "path";
	private static final String SOURCE_ATTACHMENT_PATH_ATTR = "sourcepath";	
	
	private static final String OUTPUT_KIND = "output";
	private static final String SRC_KIND = "src";
	private static final String LIB_KIND = "lib";

	public static void readBuildConfiguration(
			IProject project, JastAddJBuildConfiguration buildConfiguration) throws CoreException, IOException, SAXException, ParserConfigurationException {
		IFile rscFile = project.getFile(RESOURCE);
		if (rscFile.exists()) {
			InputStream stream = rscFile.getContents(true);

			Element cpElement;
				try {
					DocumentBuilder parser = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					cpElement = parser.parse(new InputSource(stream))
							.getDocumentElement();
				} finally {
					stream.close();
				}

				NodeList list = cpElement.getElementsByTagName(CLASSPATH_ENTRY_TAG);
			for (int i = 0; i < list.getLength(); ++i) {
				Node node = list.item(i);
				NamedNodeMap attributes = node.getAttributes();
				String kind = readAttribute(attributes, KIND_ATTR);
				if (kind == null)
					continue;

				// Try SOURCE entry
				if (kind.equals(SRC_KIND)) {
					SourcePathEntry sourcePathEntry = new SourcePathEntry();

					// PATH
					sourcePathEntry.sourcePath = readAttribute(attributes,
							PATH_ATTR);
					if (sourcePathEntry.sourcePath == null)
						continue;

					// INCLUDING attribute
					sourcePathEntry.includeList.addAll(readPatternList(
							attributes, INCLUDING_ATTR));

					// EXCLUDING attribute
					sourcePathEntry.excludeList.addAll(readPatternList(
							attributes, EXCLUDING_ATTR));

					buildConfiguration.sourcePathList.add(sourcePathEntry);
				}
				// Try LIB entry
				else if (kind.equals(LIB_KIND)) {
					ClassPathEntry classPathEntry = new ClassPathEntry();

					// PATH
					classPathEntry.classPath = readAttribute(attributes,
							PATH_ATTR);
					if (classPathEntry.classPath == null)
						continue;
					
					// SOURCE_PATH
					classPathEntry.sourceAttachmentPath = readAttribute(attributes,
							SOURCE_ATTACHMENT_PATH_ATTR);

					buildConfiguration.classPathList.add(classPathEntry);
				}
				// Try OUTPUT entry
				else if (kind.equals(OUTPUT_KIND)) {
					// PATH
					String path = readAttribute(attributes, PATH_ATTR);
					if (path == null)
						continue;
					buildConfiguration.outputPath = path;
				}
			}
		}
		else {
			// Defaults
			populateDefaults(buildConfiguration);
		}
	}
	
	public static void populateDefaults(JastAddJBuildConfiguration buildConfiguration) {
		buildConfiguration.outputPath = "bin/";
		JastAddJBuildConfiguration.SourcePathEntry sourceEntry = new JastAddJBuildConfiguration.SourcePathEntry();
		sourceEntry.sourcePath = "src/";
		buildConfiguration.sourcePathList.add(sourceEntry);		
	}

	private static List<Pattern> readPatternList(NamedNodeMap attributes,
			String attribute) {
		String includingList = readAttribute(attributes, attribute);
		List<Pattern> result = new ArrayList<Pattern>();
		if (includingList != null) {
			String[] including = includingList.split("\\|");
			for (String include : including) {
				Pattern pattern = new Pattern();
				pattern.value = include;
				result.add(pattern);
			}
		}
		return result;
	}

	protected static String readAttribute(NamedNodeMap attributes,
			String attribute) {
		Attr attr = (Attr) attributes.getNamedItem(attribute);
		if (attr != null)
			return attr.getValue();
		else
			return null;
	}

	public static void writeBuildConfiguration(IProject project,
			JastAddJBuildConfiguration buildConfiguration) throws CoreException {
		// Compose XML
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		XMLWriter xmlWriter;
		try {
			xmlWriter = new XMLWriter(byteOutputStream);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		xmlWriter.startTag(CLASSPATH_TAG, new HashMap(), true);

		// Write OUTPUT entry
		{
			HashMap<String, String> outputMap = new HashMap<String, String>();
			outputMap.put(KIND_ATTR, OUTPUT_KIND);
			outputMap.put(PATH_ATTR, buildConfiguration.outputPath);
			xmlWriter.startTag(CLASSPATH_ENTRY_TAG, outputMap, true);
			xmlWriter.endTag(CLASSPATH_ENTRY_TAG);
		}

		// Write SOURCE entries
		for (SourcePathEntry sourcePathEntry : buildConfiguration.sourcePathList) {
			HashMap<String, String> sourcePathEntryMap = new HashMap<String, String>();
			sourcePathEntryMap.put(KIND_ATTR, SRC_KIND);
			sourcePathEntryMap.put(PATH_ATTR, sourcePathEntry.sourcePath);
			if (!sourcePathEntry.includeList.isEmpty())
				sourcePathEntryMap.put(INCLUDING_ATTR,
						formatPatternList(sourcePathEntry.includeList));
			if (!sourcePathEntry.excludeList.isEmpty())
				sourcePathEntryMap.put(EXCLUDING_ATTR,
						formatPatternList(sourcePathEntry.excludeList));
			xmlWriter.startTag(CLASSPATH_ENTRY_TAG, sourcePathEntryMap, true);
			xmlWriter.endTag(CLASSPATH_ENTRY_TAG);
		}

		// Write LIB entries
		for (ClassPathEntry classPathEntry : buildConfiguration.classPathList) {
			HashMap<String, String> classPathEntryMap = new HashMap<String, String>();
			classPathEntryMap.put(KIND_ATTR, LIB_KIND);
			classPathEntryMap.put(PATH_ATTR, classPathEntry.classPath);
			if (classPathEntry.sourceAttachmentPath != null)
				classPathEntryMap.put(SOURCE_ATTACHMENT_PATH_ATTR, classPathEntry.sourceAttachmentPath);
			xmlWriter.startTag(CLASSPATH_ENTRY_TAG, classPathEntryMap, true);
			xmlWriter.endTag(CLASSPATH_ENTRY_TAG);
		}

		xmlWriter.endTag(CLASSPATH_TAG);

		xmlWriter.flush();
		xmlWriter.close();

		// Write bytes
		InputStream byteInputStream = new ByteArrayInputStream(byteOutputStream
				.toByteArray());

		IFile rscFile = project.getFile(RESOURCE);
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only file
				ResourcesPlugin.getWorkspace().validateEdit(
						new IFile[] { rscFile }, null);
			}
			rscFile.setContents(byteInputStream, IResource.FORCE, null);
		} else {
			rscFile.create(byteInputStream, IResource.FORCE, null);
		}
	}

	private static String formatPatternList(List<Pattern> list) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Pattern> i = list.iterator(); i.hasNext();) {
			buffer.append(i.next().value);
			if (i.hasNext())
				buffer.append("|");
		}
		return buffer.toString();
	}
}

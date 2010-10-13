package jester;

import java.io.File;

public class FileBasedClassIterator implements ClassIterator {
	private Configuration myConfiguration;
	private String[] myFileNames;
	private Report myReport;

	public FileBasedClassIterator(Configuration configuration, String[] fileNames, Report aReport) {
		super();
		myConfiguration = configuration;
		myFileNames = fileNames; //which is directory or file
		myReport = aReport;
	}

	public void iterate(final ClassTestTester visitor) throws SourceChangeException {
		final int[] numberOfFiles = new int[1];
		numberOfFiles[0] = 0;
		FileVisitor fileCounter = new FileVisitor() {
			public void visit(String fileName) {
				if (fileName.endsWith(myConfiguration.sourceFileExtension())) {
					numberOfFiles[0]++;
				}
			}
		};
		for (int i = 0; i < myFileNames.length; i++) {
			visitFileOrDirectory(myFileNames[i], fileCounter);		
		}
		myReport.setNumberOfFilesThatWillBeTested(numberOfFiles[0]);

		FileVisitor classTestVisitorWrapper = new FileVisitor() {
			public void visit(String fileName) throws SourceChangeException {
				if (fileName.endsWith(myConfiguration.sourceFileExtension())) {
					ClassSourceCodeChanger sourceCodeSystem = new FileBasedClassSourceCodeChanger(myConfiguration, fileName, myReport);
					visitor.testUsing(sourceCodeSystem);
				}
			}
		};

		for (int i = 0; i < myFileNames.length; i++) {
			visitFileOrDirectory(myFileNames[i], classTestVisitorWrapper);
		}
	}

	private void visitFileOrDirectory(String fileName, FileVisitor visitor) throws SourceChangeException {
		File file = new File(fileName);
		if (file.isDirectory()) {
			iterateForFilesInDirectory(file, visitor);
		} else {
			visitor.visit(fileName);
		}
	}

	private void iterateForFilesInDirectory(File directory, FileVisitor visitor) throws SourceChangeException {
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = directory.getPath() + File.separator + fileNames[i];
			visitFileOrDirectory(fileName, visitor);
		}
	}
}
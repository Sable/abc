package jester;

import java.io.*;

public class FileBasedClassSourceCodeChanger implements ClassSourceCodeChanger {
	private String sourceFileName;
	private Compiler compiler;
	private IgnoreListDocument originalContents;

	private int indexOfLastChange = -1;
	private String valueChangedFrom = "not changed";
	private String valueChangedTo = "not changed";

	private Report myReport;

	public FileBasedClassSourceCodeChanger(Configuration configuration, String sourceFileName, Report aReport) {
		super();
		this.sourceFileName = sourceFileName;
		compiler = new RealCompiler(configuration);
		myReport = aReport;
	}

	public IgnoreListDocument getOriginalContents() throws ConfigurationException {
		try {
			if (originalContents == null) {
				String ignoreListContents = "";
				try{
					ignoreListContents = Util.readFileOnClassPath(IgnoreListDocument.FILE_NAME);
				}catch(FileNotFoundException ignored){
					System.err.println("Warning - could not find "+IgnoreListDocument.FILE_NAME+" so not ignoring anything.");					
				}
				originalContents = new IgnoreListDocument(Util.readFile(sourceFileName), new IgnoreList(ignoreListContents));
			}
			return originalContents;
		} catch (IOException ex) {
			throw new ConfigurationException(ex.getMessage());
		}
	}

	public void writeOriginalContentsBack() throws SourceChangeException {
		try {
			Writer writer = new FileWriter(sourceFileName);
			getOriginalContents().writeOnto(writer, 0, getOriginalContents().length());
			writer.close();
			compiler.compile(sourceFileName);
		} catch (IOException ex) {
			throw new SourceChangeException(ex.getMessage());
		}
	}
	public void writeOverSourceReplacing(int index, String oldContents, String newContents) throws SourceChangeException {
		indexOfLastChange = index;
		valueChangedFrom = oldContents;
		valueChangedTo = newContents;
		//
		try {
			Writer writer = new FileWriter(sourceFileName);
			getOriginalContents().writeOnto(writer, 0, index);
			writer.write(newContents, 0, newContents.length());
			int afterChangeIndex = index + oldContents.length();
			getOriginalContents().writeOnto(writer, afterChangeIndex, getOriginalContents().length() - afterChangeIndex);
			writer.close();
			compiler.compile(sourceFileName);
		} catch (IOException ex) {
			throw new SourceChangeException(ex.getMessage());
		}
	}

	public void finishJesting() throws SourceChangeException {
		myReport.finishFile(sourceFileName);
	}

	public void lastChangeCausedTestsToFail() throws SourceChangeException {
		myReport.changeThatCausedTestsToFail(indexOfLastChange, valueChangedFrom, valueChangedTo);
	}

	public void lastChangeDidNotCauseTestsToFail() throws SourceChangeException {
		myReport.changeThatDidNotCauseTestsToFail(indexOfLastChange, valueChangedFrom, valueChangedTo);
	}

	public void startJesting() throws SourceChangeException {
		myReport.startFile(sourceFileName, getOriginalContents());
	}
}
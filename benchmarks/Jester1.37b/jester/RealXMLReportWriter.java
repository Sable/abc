package jester;

import java.io.*;
import java.io.IOException;
import java.io.Writer;

public class RealXMLReportWriter implements XMLReportWriter {
	private Writer myWriter;

	public RealXMLReportWriter(Writer aWriter) {
		myWriter = aWriter;
	}

	public void writeXMLReport(Object[] reportItems, String sourceFileName, int numberOfChangesThatDidNotCauseTestsToFail, int numberOfChanges, int score) throws SourceChangeException {
		try {
			String absoluteFilePath = new File(sourceFileName).getAbsolutePath();
			myWriter.write("<JestedFile fileName=\"" + sourceFileName + "\" absolutePathFileName=\"" + absoluteFilePath + "\" numberOfChangesThatDidNotCauseTestsToFail=\"" + numberOfChangesThatDidNotCauseTestsToFail + "\" numberOfChanges=\"" + numberOfChanges + "\" score=\"" + score + "\">\n");
			for (int i = 0; i < reportItems.length; i++) {
				ReportItem aReportItem = (ReportItem) reportItems[i];
				myWriter.write(aReportItem.asXML() + "\n");

			}
			myWriter.write("</JestedFile>");
		} catch (IOException ex) {
			throw new SourceChangeException(ex.toString());
		}
	}
}

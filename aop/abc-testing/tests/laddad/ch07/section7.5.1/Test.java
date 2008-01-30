//Listing 7.17 Test.java: reusing the stylesheet transformer

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class Test {
    public static void main(String[] args) throws Exception {
	printTableRaw("input1.xml");
	printTablePretty("input1.xml");
	printTableRaw("input2.xml");
	printTablePretty("input2.xml");
    }

    private static void printTableRaw(String xmlFile)
	throws TransformerConfigurationException, TransformerException {
	TransformerFactory tFactory = TransformerFactory.newInstance();
	Transformer transformer
	    = tFactory.newTransformer(
			      new StreamSource(new File("tableRaw.xsl")));
	// Use the transformer
    }

    private static void printTablePretty(String xmlFile)
	throws TransformerConfigurationException, TransformerException {
	TransformerFactory tFactory = TransformerFactory.newInstance();
	Transformer transformer
	    = tFactory.newTransformer(
			      new StreamSource(new File("tablePretty.xsl")));
	// Use the transformer
    }
}

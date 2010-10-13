import java.io.*;
import java.util.Properties;
import junit.framework.*;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.test.UnitTestManager;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * The list of test classes added to this suite was obtained by inserting a trace
 * statement into the auto-location class used by JMeter (ClassFinder).
 *
 * To compile this class, classpath must include ../build/core and
 * ../build/jorphan
 */
public class JMeterTestSuite extends TestSuite {
    public static TestSuite suite() throws ClassNotFoundException {
        initializeLogging();
        initializeManager();
        TestSuite suite = new TestSuite();
        suite.addTestSuite(Class.forName("org.apache.jmeter.gui.action.Load$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.gui.action.Save$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.testelement.property.CollectionProperty$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.testelement.property.PackageTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.testelement.PackageTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.util.StringUtilities$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.engine.util.ValueReplacer$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.engine.util.PackageTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.engine.TreeCloner$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.config.gui.ArgumentsPanel$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.control.GenericController$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.control.LoopController$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.junit.JMeterTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.threads.TestCompiler$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.save.SaveService$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.assertions.ResponseAssertion$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.control.OnceOnlyController$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.control.InterleaveControl$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.extractor.RegexExtractor$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.visualizers.StatVisualizerModel$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.junit.protocol.http.config.UrlConfigTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.junit.protocol.http.parser.HtmlParserTester"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.sampler.HTTPSampler$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.sampler.HTTPSamplerFull$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.sampler.PackageTest"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.control.CookieManager$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.modifier.AnchorModifier$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.modifier.URLRewritingModifier$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.util.HTTPArgument$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.parser.HtmlParser$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.proxy.ProxyControl$Test"));
        suite.addTestSuite(Class.forName("org.apache.jmeter.protocol.http.proxy.HttpRequestHdr$Test"));
        return suite;
    }

    /**
     * An overridable method that initializes the logging for the unit test run, using
     * the properties file passed in as the second argument.
     */
    protected static void initializeLogging() {
        Properties props = new Properties();
        try {
			System.out.println("setting up logging props using file: " + getFileName());
            props.load(new FileInputStream(getFileName()));
            LoggingManager.initializeLogging(props);
        }
        catch (FileNotFoundException e) { }
        catch (IOException e) { }
    }

	private static String getFileName() {
		String home = new File(".").getAbsolutePath();
		home = home.substring(0, home.lastIndexOf('.'));
		return home  + "jmeter.properties";
	}

    /**
     * An overridable method that that instantiates a UnitTestManager (if one was
     * specified in the command-line arguments), and hands it the name of the 
     * properties file to use to configure the system.
     */
    protected static void initializeManager() {
        try {                
            UnitTestManager um = (UnitTestManager)Class.forName("org.apache.jmeter.util.JMeterUtils").newInstance();
            um.initializeProperties(getFileName());
        }
        catch (Exception e)  {
            System.out.println("Couldn't create: "+"org.apache.jmeter.util.JMeterUtils");
            e.printStackTrace();
        }
    }
}

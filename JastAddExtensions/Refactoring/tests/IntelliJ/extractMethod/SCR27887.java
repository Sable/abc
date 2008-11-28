package extractMethod;

import java.io.*;
import java.util.Iterator;

public class SCR27887 {
    
    // following lines added to make it compile
    static boolean VERBOSE;
    class Subsystem { String refQualifiedIdentifyingName() { return null; } 
  	              Iterable getModule() { return null; } }
    Subsystem subsystem;
    class ZippingXMLGeneratorFactory { ZippingXMLGeneratorFactory(OutputStream out) { } 
	                               OutputStream getOutputStream(String filename) { return null; }
	                               void close() { } }
    class ScatteringDocBuilder { }
    class MyDocBuilder extends ScatteringDocBuilder { MyDocBuilder(Object repository, Object included) { } }
    class RefObjectUList { boolean isEmpty() { return false; } 
	                   int size() { return 0; } }
    RefObjectUList makeIncludedSet() { return null; }
    Object repository;
    class RepositorySaver { RepositorySaver(Object repository) { } 
	                    void saveTo(ZippingXMLGeneratorFactory x, ScatteringDocBuilder y) { } }
    class OptimalModule { boolean getPublished() { return false; } }
    class FileObject { String getFileName() { return null; } 
  	               InputStream getInputStream() { return null; } }
    void copyStream(InputStream is, OutputStream os) { }
    FileObject[] getModuleProducts(OptimalModule o) { return null; }
    // up to here

    public int publishx(OutputStream out, boolean includeCode) throws IOException {
        if (VERBOSE) System.err.println("PUBLISH: publishing subsystem '" + subsystem.refQualifiedIdentifyingName() + "' with" + (includeCode ? "" : "out") + " code");
        ZippingXMLGeneratorFactory genFac = new ZippingXMLGeneratorFactory(out);
	//========
        /*[*/RefObjectUList included = makeIncludedSet();
        if (!included.isEmpty()) {
            ScatteringDocBuilder docBuilder = new MyDocBuilder(repository, included);
            new RepositorySaver(repository).saveTo(genFac, docBuilder);
        }/*]*/
	//========
        if (includeCode) {
            for (Iterator i = subsystem.getModule().iterator(); i.hasNext();) {
                OptimalModule module = (OptimalModule) i.next();
                if (module.getPublished()) {
                    FileObject[] files = getModuleProducts(module);
                    if (files != null && files.length > 0) {
                        for (int j = 0; j < files.length; j++) {
                            FileObject file = files[j];
                            OutputStream os = genFac.getOutputStream(file.getFileName());
                            InputStream is = file.getInputStream();
                            try {
                                copyStream(is, os);
                            } finally {
                                os.close();
                                is.close();
                            }
                        }
                    }
                }
            }
        }
        genFac.close();
        return included.size();
    }
}

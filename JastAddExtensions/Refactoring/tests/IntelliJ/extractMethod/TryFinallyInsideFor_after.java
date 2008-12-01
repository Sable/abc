package extractMethod;

import java.io.*;
import java.util.Iterator;

/*public*/ class SCR27887 {
    public int publishx(OutputStream out, boolean includeCode) throws IOException {
	// following two lines added to make it compile
	Object repository = null;
	Object included = null;
        newMethod(repository, included);
        while(true){
          OutputStream os = null;
          try {
          } finally {
            os.close();
          }
        }
    }

    private void newMethod(Object repository, Object included) {
        ScatteringDocBuilder docBuilder = new MyDocBuilder(repository, included);
    }
}

// the following added to make it compile
class ScatteringDocBuilder { }

class MyDocBuilder extends ScatteringDocBuilder {
    public MyDocBuilder(Object repository, Object included) {
    }
}
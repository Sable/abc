import java.io.OutputStream;

class A {
    {
        try {
            OutputStream out = null;

            /*[*/try {
            } finally {
                out.close();
            }/*]*/
        } catch(Throwable t) {
        }
    }
}
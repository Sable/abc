public class Try {
    public int test() {
        int i = another();
        return i;
    }
 
    /*[*/public synchronized int another() {
        try {
            return Integer.parseInt("1");
        }
        catch (NumberFormatException ex) {
            throw ex;
        }
    }/*]*/
}

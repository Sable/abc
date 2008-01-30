//Listing 8.6 RunnableWithReturn.java

package pattern.worker;

public abstract class RunnableWithReturn implements Runnable {
    protected Object _returnValue;

    public Object getReturnValue() {
	return _returnValue;
    }
}

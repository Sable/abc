package org.aspectbench.runtime.internal;

/**
 * @author Damien Sereni
 */
public class CflowCounterFactory {

	public static CflowCounterInterface makeCflowCounter() {
		if (DecideThreadLocal.ok())
			return new CflowCounterThreadLocal();
		else
			return new CflowCounterGlobal();
	}
	
}

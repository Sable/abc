package org.aspectbench.runtime.internal;

import org.aspectbench.runtime.internal.cflowinternal.Counter;

/**
 * @author Damien Sereni
 */
public interface CflowCounterInterface {

	public Counter getThreadCounter();
	
}

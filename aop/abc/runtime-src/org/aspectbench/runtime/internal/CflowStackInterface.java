package org.aspectbench.runtime.internal;

import org.aspectbench.runtime.internal.cflowinternal.*;

/**
 * @author Damien Sereni
 */
public interface CflowStackInterface {

	public interface Ref {
		public StackRef getThreadStack();
	}
	public interface Int {
		public StackInt getThreadStack();
	}
	public interface Long {
		public StackLong getThreadStack();
	}
	public interface Float {
		public StackFloat getThreadStack();
	}
	public interface Double {
		public StackDouble getThreadStack();
	}
	
}

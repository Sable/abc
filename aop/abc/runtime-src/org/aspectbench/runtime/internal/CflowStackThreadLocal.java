package org.aspectbench.runtime.internal;

import org.aspectbench.runtime.internal.cflowinternal.*;

/**
 * @author Damien Sereni
 */
public class CflowStackThreadLocal {

	public static class Ref extends ThreadLocal implements CflowStackInterface.Ref {
		private Thread first_thread = Thread.currentThread();
		private StackRef first_stack = new StackRef();
		public Object initialValue() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return new StackRef();
		}
		public StackRef getThreadStack() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return (StackRef)get();
		}
	}
	public static class Int extends ThreadLocal implements CflowStackInterface.Int {
		private Thread first_thread = Thread.currentThread();
		private StackInt first_stack = new StackInt();
		public Object initialValue() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return new StackRef();
		}
		public StackInt getThreadStack() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return (StackInt)get();
		}
	}
	public static class Long extends ThreadLocal implements CflowStackInterface.Long {
		private Thread first_thread = Thread.currentThread();
		private StackLong first_stack = new StackLong();
		public Object initialValue() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return new StackRef();
		}
		public StackLong getThreadStack() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return (StackLong)get();
		}
	}
	public static class Float extends ThreadLocal implements CflowStackInterface.Float {
		private Thread first_thread = Thread.currentThread();
		private StackFloat first_stack = new StackFloat();
		public Object initialValue() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return new StackRef();
		}
		public StackFloat getThreadStack() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return (StackFloat)get();
		}
	}
	public static class Double extends ThreadLocal implements CflowStackInterface.Double {
		private Thread first_thread = Thread.currentThread();
		private StackDouble first_stack = new StackDouble();
		public Object initialValue() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return new StackRef();
		}
		public StackDouble getThreadStack() {
			if (Thread.currentThread() == first_thread) return first_stack;
			return (StackDouble)get();
		}
	}
	
}

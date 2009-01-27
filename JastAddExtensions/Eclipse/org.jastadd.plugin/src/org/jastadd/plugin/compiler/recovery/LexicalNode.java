package org.jastadd.plugin.compiler.recovery;

public abstract class LexicalNode {

	private LexicalNode previous;
	private LexicalNode next;
	private Interval interval;
	private String value;

	public LexicalNode(LexicalNode previous, Interval interval, String value) {
		this.previous = previous;
		this.interval = interval;
		this.value = value;
		if (previous != null) {
			previous.next = this;
		}
	}

	public void setNext(LexicalNode node) {
		next = node;
	}
	public LexicalNode getNext() {
		return next;
	}
	public void setPrevious(LexicalNode node) {
		previous = node;
	}
	public LexicalNode getPrevious() {
		return previous;
	}
	public Interval getInterval() {
		return interval;
	}
	public String getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public LexicalNode getPreviousOfType(Class clazz) {
		/* DEBUG System.out.println("\t\tTesting " + this + " for class " + clazz.getName()); */
		if (getClass().getName().equals(clazz.getName())) {
			return this;
		} else if (previous != null) {
			return previous.getPreviousOfType(clazz);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public LexicalNode getNextOfType(Class clazz) {
		if (getClass().getName().equals(clazz.getName())) {
			return this;
		} else if (next != null) {
			return next.getNextOfType(clazz);
		}
		return null;
	}

	public String toString() {
		String s = value.replaceAll("\n", "<NEWLINE>").replaceAll("\t", "<TAB>");
		return "(" + s + ") " + interval;
	}

	public boolean includeInPrettyPrint() {
		return true;
	}

	public abstract LexicalNode clone(LexicalNode previous);

	@SuppressWarnings("unchecked")
	public LexicalNode getPreviousOfType(Class clazz, int distance) {
		if (distance <= 0)
			return null;
		if (getClass().getName().equals(clazz.getName())) {
			return this;
		} else if (previous != null) {
			return previous.getPreviousOfType(clazz, distance - 1);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public LexicalNode getNextOfType(Class clazz, int distance) {
		if (distance <= 0) 
			return null;
		if (getClass().getName().equals(clazz.getName())) {
			return this;
		} else if (next != null) {
			return next.getNextOfType(clazz, distance - 1);
		}
		return null;
	}
}

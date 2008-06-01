package org.jastadd.plugin.model.repair;

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

	public LexicalNode getPreviousOfType(Class clazz) {
		/* DEBUG System.out.println("\t\tTesting " + this + " for class " + clazz.getName()); */
		if (getClass().getName().equals(clazz.getName())) {
			return this;
		} else if (previous != null) {
			return previous.getPreviousOfType(clazz);
		}
		return null;
	}
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
}

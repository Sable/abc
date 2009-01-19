package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.*;

public class RightParen extends Island {
	public RightParen(LexicalNode previous, Interval interval) {
		super(previous, interval, ")");
	}

	public boolean bridgeMatch(Island island, int tol) {
		return tol <= 0 &&
			island instanceof LeftParen &&
			(((LeftParen)island).indent().equalTo(indent()) ||
			((LeftParen)island).indent().lessThan(indent()));
	}	
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge()) {
			bridge = new ParenBridge((LeftParen)target, this);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "RParan\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new RightParen(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		return (node instanceof Indent && ((Indent)node).lessThan(indent()));
	}
	public Island constructFakeIsland(LexicalNode node, boolean intervalEnd) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
		Island island = new LeftParen(null, interval);
		island.setFake(true);
		return island;
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertAfter(island, node);
	}
	public boolean startOfBridge() {
		return false;
	}

	public static final char[] TOKEN = {')'};
	protected Indent indent;

	public Indent indent() {
		if (indent == null) {
			LexicalNode node = getPrevious().getPreviousOfType(Indent.class);
			if (node != null) {
				indent = (Indent)node;
			} else {
				SOF sof = (SOF)getPrevious().getPreviousOfType(SOF.class);
				indent = new Indent(null, sof.getInterval(), "", false);
				Recovery.insertAfter(indent, sof);
			}
		}
		return indent;
	}
}

package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.*;

public class LeftParen extends Island {
	public LeftParen(LexicalNode previous, Interval interval) {
		super(previous, interval, "(");
	}

	public boolean bridgeMatch(Island island, int tol) {
		return tol <= 0 && 
			island instanceof RightParen &&
			(((RightParen)island).indent().equalTo(indent()) ||
			indent().lessThan(((RightParen)island).indent()));
	}	
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge()) {
			bridge = new ParenBridge(this, (RightParen)target);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "LParen\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new LeftParen(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		if (node instanceof Indent) {
			Indent ind = (Indent)node;
			return ind.equalTo(indent()) || ind.lessThan(indent());
		}
		return node instanceof Island;
	}
	public Island constructFakeIsland(LexicalNode node, boolean intervalEnd) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getEnd(), nodeInterval.getEnd());
		Island island = new RightParen(null, interval);
		island.setFake(true);
		return island;
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertBefore(island, node);
	}
	public boolean startOfBridge() {
		return true;
	}


	public static final char[] TOKEN = {'('};
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

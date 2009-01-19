package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Bridge;
import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.Island;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.SOF;

public class RightBrace extends Island {
	public RightBrace(LexicalNode previous, Interval interval) {
		super(previous, interval, "}");
	}

	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof LeftBrace && 
			(((LeftBrace)island).indent().equalTo(indent()) ||
			indent().lessThan(((LeftBrace)island).indent(), 1));
	}
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge()) {
			bridge = new BraceBridge((LeftBrace)target, this);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "RBrace\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new RightBrace(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		/* DEBUG System.out.println("RightBrace: possible match node = " + node); */
		if (node instanceof Indent) {
			Indent ind = (Indent)node;
			return (ind.lessThan(indent()) || ind.equalTo(indent())) &&
					ind != indent() && !(ind.getNext() instanceof Indent);
		}
		else if (node instanceof LeftBrace) {
			LeftBrace lb = (LeftBrace)node;
			return !lb.hasBridge() && indent().lessThan(lb.indent()); 
		}
		return false;
	}
	public Island constructFakeIsland(LexicalNode node, boolean intervalEnd) {
		if (!intervalEnd && node instanceof LeftBrace) {
			return (LeftBrace)node;
		} else {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new LeftBrace(null, interval);
			island.setFake(true);
			return island;
		}
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		if (island.isFake()) {
			if (node instanceof Indent) {
				Indent ind = (Indent)node.getNext().getNextOfType(Indent.class);
				if (ind != indent()) {
					Recovery.insertBefore(island, ind);
					return;
				}
			}
			Recovery.insertAfter(island, node);
		}
	}
	public boolean startOfBridge() {
		return false;
	}


	public static final char[] TOKEN = {'}'};
	private Indent indent;

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
